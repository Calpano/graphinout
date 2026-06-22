package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ddot.it {@code ddot.it/label} command (and its {@code !!label} shorthand) sets a node's display
 * label rather than creating an edge; the {@code !!off}/{@code !!on} shorthands mute/resume emission.
 * See https://ddot.it/label and the {@code 13-label} / {@code 11-shorthand-command} corpus cases.
 */
class DDotLabelCommandTest {

    private static ICjGraph graph(String ddot) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
        return doc.graphs().findFirst().orElseThrow();
    }

    private static String labelOf(ICjGraph g, String nodeId) {
        ICjNode n = g.nodes().filter(x -> nodeId.equals(x.id())).findFirst().orElseThrow();
        ICjLabel l = n.label();
        return l == null ? null : l.entries().map(ICjLabelEntry::value).findFirst().orElse(null);
    }

    @Test
    void shorthandLabelSetsNodeLabelNotEdge() throws IOException {
        // the canonical https://ddot.it/label example
        ICjGraph g = graph("""
                a ..knows.. b
                b ..knows.. c
                a ..!!label.. Alice
                b ..!!label.. Bob
                c ..!!label.. Carol
                """);
        // exactly the three real nodes — no phantom "Alice"/"Bob"/"Carol" nodes
        assertEquals(3, g.nodes().count(), "only a, b, c");
        // exactly the two real edges — the !!label lines are NOT edges
        assertEquals(2, g.edges().count(), "only the two knows-edges");
        assertEquals("Alice", labelOf(g, "a"));
        assertEquals("Bob", labelOf(g, "b"));
        assertEquals("Carol", labelOf(g, "c"));
    }

    @Test
    void nativeLabelCommandSetsNodeLabel() throws IOException {
        ICjGraph g = graph("x ..ddot.it/label.. Hello World\n");
        assertEquals(1, g.nodes().count());
        assertEquals(0, g.edges().count());
        assertEquals("Hello World", labelOf(g, "x"));
    }

    @Test
    void offOnShorthandMutesEmission() throws IOException {
        ICjGraph g = graph("""
                a ..knows.. b
                !!off
                muted ..relation.. here
                !!on
                c ..knows.. d
                """);
        assertEquals(2, g.edges().count(), "the muted line must not become an edge");
        assertTrue(g.nodes().noneMatch(n -> "muted".equals(n.id())), "no node from the muted line");
    }
}
