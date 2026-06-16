package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trips Adjacency List: read -> CjDocument -> write -> read again, asserting the graph (node ids + directed
 * edge pairs, including an isolated node) survives, and that a second write equals the first.
 */
class AdjListRoundtripTest {

    @Test
    void roundtripAdjList() throws IOException {
        // 'd' is an isolated node (its own line, no neighbours)
        String input = "a b c\nb c\nd\n";

        ICjDocument doc1 = read(input);
        String out1 = AdjListWriter.toAdjList(doc1);
        ICjDocument doc2 = read(out1);
        String out2 = AdjListWriter.toAdjList(doc2);

        assertEquals(out1, out2, "second serialization should equal the first");
        assertEquals(structure(doc1), structure(doc2), "graph structure should survive the round-trip");
        // isolated node 'd' must be preserved
        assertEquals(true, structure(doc1).contains("d"), "isolated node should be present");
    }

    private static ICjDocument read(String content) throws IOException {
        AdjListReader reader = new AdjListReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.adjlist", content), new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
    }

    private static String structure(ICjDocument doc) {
        TreeSet<String> nodes = new TreeSet<>();
        doc.nodesAllIncludingImplied().forEach(n -> nodes.add(n.id()));
        List<String> edges = new ArrayList<>();
        doc.edgesAll().forEach(e -> {
            String[] st = TextEndpoints.sourceTarget(e.endpoints().toList());
            if (st != null) edges.add(st[0] + "->" + st[1]);
        });
        edges.sort(String::compareTo);
        return "nodes=" + nodes + " edges=" + edges;
    }

}
