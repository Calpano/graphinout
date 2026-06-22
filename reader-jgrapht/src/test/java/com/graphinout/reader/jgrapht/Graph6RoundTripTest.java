package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end round-trip across the full graphinout pipeline: a graph6 string is read into the CJ pivot model
 * by {@link Graph6Reader} and written back out by {@link Graph6Writer}, and the resulting bytes must equal the
 * original. Also covers the CJ &rarr; graph6 &rarr; CJ direction.
 */
class Graph6RoundTripTest {

    /** graph6 string -> CJ -> graph6 string. */
    private String roundTrip(String graph6) throws IOException {
        Graph6Reader reader = new Graph6Reader();
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("t.g6", graph6 + "\n"));
        assertNotNull(doc);
        InMemoryOutputSink sink = new InMemoryOutputSink();
        new Graph6Writer().writeCjDocument(doc, sink);
        return sink.getBufferAsUtf8String().trim();
    }

    @Test
    void specExampleRoundTrips() throws IOException {
        assertEquals("DQc", roundTrip("DQc"));
    }

    @Test
    void variousGraphsRoundTrip() throws IOException {
        for (String s : new String[]{"@", "A?", "A_", "BW", "DF{", "D]w", "E?Bw"}) {
            assertEquals(s, roundTrip(s), "graph6 -> CJ -> graph6 for '" + s + "'");
        }
    }

    @Test
    void nodeAndEdgeCountsSurviveRead() throws IOException {
        Graph6Reader reader = new Graph6Reader();
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("t.g6", "DF{\n"));
        assertNotNull(doc);
        assertEquals(5, doc.nodesAll().count(), "DF{ has 5 vertices");
        assertEquals(7, doc.edgesAll().count(), "DF{ has 7 edges");
    }

    @Test
    void multiGraphFileBecomesMultipleCjGraphs() throws IOException {
        Graph6Reader reader = new Graph6Reader();
        // three graphs, one per line
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of("multi.g6", "A_\nBW\nDF{\n"));
        assertNotNull(doc);
        assertEquals(3, doc.graphs().count(), "one CJ graph per graph6 line");

        // and they re-emit as three lines
        InMemoryOutputSink sink = new InMemoryOutputSink();
        new Graph6Writer().writeCjDocument(doc, sink);
        String[] lines = sink.getBufferAsUtf8String().trim().split("\n");
        assertEquals(3, lines.length);
        assertEquals("A_", lines[0]);
        assertEquals("BW", lines[1]);
        assertEquals("DF{", lines[2]);
    }

    @Test
    void headerIsStripped() throws IOException {
        assertEquals("DQc", roundTrip(">>graph6<<DQc"));
    }
}
