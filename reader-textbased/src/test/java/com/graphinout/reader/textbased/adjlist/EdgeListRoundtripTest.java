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
 * Round-trips Edge List: read -> CjDocument -> write -> read again, asserting the graph (node ids + directed edge
 * pairs) survives, and that a second write equals the first (stable serialization).
 */
class EdgeListRoundtripTest {

    @Test
    void roundtripEdgeList() throws IOException {
        String input = "a b\nb c\na c\n";

        ICjDocument doc1 = read(input);
        String out1 = EdgeListWriter.toEdgeList(doc1);
        ICjDocument doc2 = read(out1);
        String out2 = EdgeListWriter.toEdgeList(doc2);

        // stable serialization
        assertEquals(out1, out2, "second serialization should equal the first");
        // structural equivalence across the round-trip
        assertEquals(structure(doc1), structure(doc2), "graph structure should survive the round-trip");
        // the written edge list reproduces the input edges (order-independent)
        assertEquals(sortedLines(input), sortedLines(out1), "edges should be preserved");
    }

    private static ICjDocument read(String content) throws IOException {
        EdgeListReader reader = new EdgeListReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.edgelist", content), new CjStream2CjWriter(docWriter, true));
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

    private static String sortedLines(String s) {
        return String.join("\n", s.lines().map(String::trim).filter(l -> !l.isEmpty()).sorted().toList());
    }

}
