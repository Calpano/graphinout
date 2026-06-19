package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CjDocumentAnonymizerTest {

    private final IJsonFactory jf = IJsonFactory.INSTANCE;

    // ----------------------------------------------------------------- char rule

    @Test
    void charRulePreservesShape() {
        assertEquals("Xxxxx Xxxxx 00!", Anonymizer.text("Hello World 42!"));
        assertEquals("Xxx-xxx_X.x/0", Anonymizer.text("Abc-def_G.h/9"));
        // non-ASCII: cased letters mapped (Ü->X, é->x), case-less kept as letters too
        assertEquals("Xxxx xxxX", Anonymizer.text("Über cafÉ"));
    }

    // ----------------------------------------------------------------- document

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
        ICjDocument anon = CjDocumentAnonymizer.anonymize(sampleDoc());

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
        List<String> endpointNodes = edge.endpoints().map(ep -> ep.node()).toList();
        assertEquals(List.of("node1", "node2"), endpointNodes);
        assertEquals(0, edge.data().jsonValue().asObject().get("key5").asNumber().intValue());
    }

    @Test
    void sameKeyIsRemappedConsistentlyAcrossElements() {
        // two nodes each with a "shared" key -> both must map to the same synthetic key
        IJsonObjectMutable a = jf.createObjectMutable();
        a.add("shared", "x");
        IJsonObjectMutable b = jf.createObjectMutable();
        b.add("shared", "y");
        CjDocumentElement doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n -> { n.id("a"); n.dataMutable(d -> d.setJsonValue(a)); });
            g.addNode(n -> { n.id("b"); n.dataMutable(d -> d.setJsonValue(b)); });
        });

        ICjDocument anon = CjDocumentAnonymizer.anonymize(doc);
        List<ICjNode> nodes = anon.graphs().findFirst().orElseThrow().nodes().toList();
        IJsonObject d0 = nodes.get(0).data().jsonValue().asObject();
        IJsonObject d1 = nodes.get(1).data().jsonValue().asObject();
        assertEquals(java.util.Set.of("key1"), d0.keys());
        assertEquals(java.util.Set.of("key1"), d1.keys(), "same original key -> same synthetic key");
    }

    private static String labelText(ICjNode n) {
        return labelText(n.label());
    }

    private static String labelText(com.graphinout.base.cj.document.ICjLabel label) {
        assertNotNull(label);
        return label.entries().findFirst().orElseThrow().value();
    }
}
