package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code ddot.it/block} (shorthand {@code !!block}) makes the object a multi-line literal: the lines that
 * follow, joined by newlines, until the next statement. See https://ddot.it/block.
 */
class DDotBlockTest {

    private static ICjGraph graph(String ddot) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
        return doc.graphs().findFirst().orElseThrow();
    }

    private static String targetOf(ICjGraph g, String type) {
        ICjEdge e = g.edges().filter(x -> type.equals(x.type())).findFirst().orElseThrow();
        return e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElseThrow().node();
    }

    @Test
    void blockBecomesMultiLineObjectValue() throws IOException {
        ICjGraph g = graph("""
                john ..address.. ddot.it/block
                Broadway 1
                Berlin
                Germany
                john ..age.. 11
                """);
        assertEquals(2, g.edges().count(), "address + age");
        assertEquals("Broadway 1\nBerlin\nGermany", targetOf(g, "address"), "multi-line block value");
        assertEquals("11", targetOf(g, "age"), "the triple after the block is parsed normally");
    }

    @Test
    void shorthandBlockAlsoWorks() throws IOException {
        ICjGraph g = graph("""
                x ..note.. !!block
                line one
                line two
                """);
        assertEquals("line one\nline two", targetOf(g, "note"));
    }
}
