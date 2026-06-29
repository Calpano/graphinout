package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The streaming anonymizer redacts content and remaps ids/keys consistently — keeping structure and links — without
 * buffering the graph. (Equivalence with the former whole-document anonymizer was verified across the whole
 * graph-test-data corpus before that class was removed.)
 */
class AnonymizingCjStreamTest {

    private final IJsonFactory jf = IJsonFactory.INSTANCE;

    /** Anonymize a document by streaming it through {@link AnonymizingCjStream} and capturing the result. */
    private static ICjDocument anonymize(ICjDocument source) {
        CjWriter2CjDocumentWriter capture = new CjWriter2CjDocumentWriter();
        AnonymizingCjStream anon = new AnonymizingCjStream(new CjStream2CjWriter(capture, false));
        CjDocument2CjStream.toCjStream(source, anon);
        return capture.resultDoc();
    }

    // ----------------------------------------------------------------- structure / links / no leakage

    private static final String SOURCE = """
            {"graphs":[{"nodes":[
                {"id":"p1","types":["Person"],"label":{"entries":[{"value":"Alice"}]},"data":{"age":42}},
                {"id":"p2","types":["Person"]}
              ],"edges":[
                {"type":"knows","endpoints":[{"node":"p1","direction":"in"},{"node":"p2","direction":"out"}]}
              ]}]}
            """;

    @Test
    void streamsAnonymizationKeepingStructureAndLinks() throws IOException {
        ICjDocument result = anonymize(CjDocuments.parseCjJsonString("src", SOURCE));

        CjAnalysis a = CjAnalyzer.analyze(result);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);

        String json = CjDocuments.toJsonString(result);
        for (String secret : new String[]{"p1", "p2", "Person", "Alice", "knows", "age", "42"}) {
            assertThat(json).doesNotContain(secret);
        }

        Set<String> nodeIds = CjFeature.allNodes(result).map(ICjNode::id).collect(Collectors.toSet());
        assertThat(nodeIds).containsExactly("node1", "node2");
        CjFeature.allEdges(result).flatMap(ICjEdge::endpoints).map(ICjEndpoint::node)
                .forEach(n -> assertThat(nodeIds).contains(n));
    }

    // ----------------------------------------------------------------- labels / types / ids / data

    private ICjDocument sampleDoc() {
        IJsonObjectMutable a = jf.createObjectMutable();
        a.add("name", "Alice");
        a.add("age", 42);
        a.add("active", true);
        IJsonObjectMutable b = jf.createObjectMutable();
        b.add("city", "Berlin");
        IJsonObjectMutable e = jf.createObjectMutable();
        e.add("since", 1999);

        CjDocumentElement doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.id("g-secret");
            g.addNode(n -> {
                n.id("alice");
                n.setLabel(l -> l.addEntry(en -> en.value("Alice Smith")));
                n.addType(ICjElementType.of("Person"));
                n.dataMutable(d -> d.setJsonValue(a));
            });
            g.addNode(n -> {
                n.id("bob");
                n.setLabel(l -> l.addEntry(en -> en.value("Bob")));
                n.dataMutable(d -> d.setJsonValue(b));
            });
            g.addEdge(ed -> {
                ed.id("knows-1");
                ed.edgeType(ICjElementType.of("KNOWS"));
                ed.setLabel(l -> l.addEntry(en -> en.value("knows well")));
                ed.addEndpoint(ep -> ep.node("alice").direction(CjDirection.IN));
                ed.addEndpoint(ep -> ep.node("bob").direction(CjDirection.OUT));
                ed.dataMutable(d -> d.setJsonValue(e));
            });
        });
        return doc;
    }

    @Test
    void anonymizesLabelsTypesIdsAndData() {
        ICjDocument anon = anonymize(sampleDoc());

        ICjGraph g = anon.graphs().findFirst().orElseThrow();
        assertEquals("graph1", g.id(), "graph id remapped");

        List<ICjNode> nodes = g.nodes().toList();
        ICjNode n1 = nodes.get(0);
        ICjNode n2 = nodes.get(1);
        assertEquals("node1", n1.id());
        assertEquals("node2", n2.id());

        // label text anonymized, spacing preserved
        assertEquals("Xxxxx Xxxxx", labelText(n1));
        assertEquals("Xxx", labelText(n2));

        // node type anonymized
        assertEquals("Xxxxxx", n1.types().findFirst().orElseThrow().type());

        // data: keys remapped consistently (sorted -> active=key1, age=key2, name=key3),
        // string values anonymized, number zeroed, bool kept
        IJsonObject d1 = n1.data().jsonValue().asObject();
        assertTrue(d1.get("key1").asBoolean(), "active: boolean kept");
        assertEquals(0, d1.get("key2").asNumber().intValue(), "age zeroed");
        assertEquals("Xxxxx", d1.get("key3").asString(), "name 'Alice' anonymized");

        // node b's 'city' gets the next synthetic key (key4)
        IJsonObject d2 = n2.data().jsonValue().asObject();
        assertEquals("Xxxxxx", d2.get("key4").asString(), "value of city (Berlin)");

        // edge: id + type anonymized, endpoints remapped to the SAME node ids -> links intact
        ICjEdge edge = g.edges().findFirst().orElseThrow();
        assertEquals("edge1", edge.id());
        assertEquals("XXXXX", edge.edgeType().type());
        assertEquals("xxxxx xxxx", labelText(edge.label()));
        List<String> endpointNodes = edge.endpoints().map(ICjEndpoint::node).toList();
        assertEquals(List.of("node1", "node2"), endpointNodes);
        assertEquals(0, edge.data().jsonValue().asObject().get("key5").asNumber().intValue());
    }

    @Test
    void sameKeyIsRemappedConsistentlyAcrossElements() {
        IJsonObjectMutable a = jf.createObjectMutable();
        a.add("shared", "x");
        IJsonObjectMutable b = jf.createObjectMutable();
        b.add("shared", "y");
        CjDocumentElement doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n -> { n.id("a"); n.dataMutable(d -> d.setJsonValue(a)); });
            g.addNode(n -> { n.id("b"); n.dataMutable(d -> d.setJsonValue(b)); });
        });

        ICjDocument anon = anonymize(doc);
        List<ICjNode> nodes = anon.graphs().findFirst().orElseThrow().nodes().toList();
        IJsonObject d0 = nodes.get(0).data().jsonValue().asObject();
        IJsonObject d1 = nodes.get(1).data().jsonValue().asObject();
        assertEquals(Set.of("key1"), d0.keys());
        assertEquals(Set.of("key1"), d1.keys(), "same original key -> same synthetic key");
    }

    private static String labelText(ICjNode n) {
        return labelText(n.label());
    }

    private static String labelText(ICjLabel label) {
        assertNotNull(label);
        return label.entries().findFirst().orElseThrow().value();
    }
}
