package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * digraph6 arcs keep their direction: the adjacency bit at row i, column j is the arc i -> j, which becomes a CJ edge
 * with source i (endpoint direction "in") and target j ("out").
 */
class Digraph6DirectionTest {

    private static List<String> arcs(String digraph6) throws IOException {
        ICjDocument doc = new Digraph6Reader().readToCjDocument(SingleInputSource.of("test.d6", digraph6));
        return doc.edgesAll().map(ICjEdge.class::cast)
                .map(e -> e.source().node() + "->" + e.target().node()).sorted().collect(Collectors.toList());
    }

    @Test
    void singleArc() throws IOException {
        // n=2 ('A' = 2+63), bits row-major 01|00 padded to 010000 = 16 -> 'O' (16+63): the arc 0 -> 1
        assertEquals(List.of("0->1"), arcs("&AO"));
    }

    @Test
    void reverseArcIsADifferentGraph() throws IOException {
        // bits 00|10 padded to 001000 = 8 -> 'G': the arc 1 -> 0
        assertEquals(List.of("1->0"), arcs("&AG"));
    }

    @Test
    void cycleOfThree() throws IOException {
        // n=3 ('B'); arcs 0->1, 1->2, 2->0: bits 010|001|100 = 010001 100000 -> 17 'P', 32 '_'
        assertEquals(List.of("0->1", "1->2", "2->0"), arcs("&BP_"));
    }
}
