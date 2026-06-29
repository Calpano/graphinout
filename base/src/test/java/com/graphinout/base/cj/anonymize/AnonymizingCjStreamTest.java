package com.graphinout.base.cj.anonymize;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

/** The streaming anonymizer redacts content and remaps ids consistently, keeping structure and links — without buffering the graph. */
class AnonymizingCjStreamTest {

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
        ICjDocument source = CjDocuments.parseCjJsonString("src", SOURCE);

        CjWriter2CjDocumentWriter out = new CjWriter2CjDocumentWriter();
        AnonymizingCjStream anon = new AnonymizingCjStream(new CjStream2CjWriter(out, false));
        CjDocument2CjStream.toCjStream(source, anon);
        ICjDocument result = out.resultDoc();

        // structure preserved
        CjAnalysis a = CjAnalyzer.analyze(result);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);

        // no original identifiers / labels / types / values leak
        String json = CjDocuments.toJsonString(result);
        for (String secret : new String[]{"p1", "p2", "Person", "Alice", "knows", "age", "42"}) {
            assertThat(json).doesNotContain(secret);
        }

        // ids remapped on first encounter, and links stay consistent (every endpoint references a declared node id)
        Set<String> nodeIds = CjFeature.allNodes(result).map(ICjNode::id).collect(Collectors.toSet());
        assertThat(nodeIds).containsExactly("node1", "node2");
        CjFeature.allEdges(result).flatMap(ICjEdge::endpoints).map(ICjEndpoint::node)
                .forEach(n -> assertThat(nodeIds).contains(n));
    }
}
