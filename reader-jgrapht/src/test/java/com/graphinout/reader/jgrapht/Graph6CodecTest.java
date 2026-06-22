package com.graphinout.reader.jgrapht;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Graph6Codec} against ground-truth byte strings and worked examples from the nauty
 * specification (<a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">formats.txt</a>).
 */
class Graph6CodecTest {

    private static String n(int value) {
        StringBuilder sb = new StringBuilder();
        Graph6Codec.appendN(sb, value);
        return sb.toString();
    }

    // ---------------------------------------------------------------- N(n) encoding

    @Test
    void nEncodingSmall() {
        assertEquals("?", n(0), "N(0) is byte 63");
        assertEquals("@", n(1), "N(1) is byte 64");
        assertEquals("}", n(62), "N(62) is byte 125");
    }

    @Test
    void nEncodingSpecExamples() {
        // N(30) = 93
        assertEquals(93, n(30).charAt(0));
        // N(12345) = 126 66 63 120
        String s = n(12345);
        assertEquals(4, s.length());
        assertEquals(126, s.charAt(0));
        assertEquals(66, s.charAt(1));
        assertEquals(63, s.charAt(2));
        assertEquals(120, s.charAt(3));
    }

    @Test
    void nEncodingRoundTripsAcrossBoundaries() {
        for (int v : new int[]{0, 1, 61, 62, 63, 64, 258047, 258048, 1_000_000}) {
            Graph6Codec.Cursor cursor = new Graph6Codec.Cursor();
            assertEquals(v, Graph6Codec.readN(n(v), cursor), "N round-trip for " + v);
        }
    }

    // ---------------------------------------------------------------- graph6

    @Test
    void graph6SpecWorkedExample() {
        // formats.txt: n=5, edges {0-2, 0-4, 1-3, 3-4} encodes to "DQc".
        Graph6Codec.Graph g = Graph6Codec.decodeGraph6("DQc");
        assertEquals(5, g.n());
        assertEquals(List.of(
                new Graph6Codec.Edge(0, 2),
                new Graph6Codec.Edge(1, 3),
                new Graph6Codec.Edge(0, 4),
                new Graph6Codec.Edge(3, 4)
        ), g.edges());
        assertEquals("DQc", Graph6Codec.encodeGraph6(g.n(), g.edges()), "re-encode matches spec bytes");
    }

    @Test
    void graph6Edges() {
        assertEquals(1, Graph6Codec.decodeGraph6("@").n(), "'@' is the empty graph on 1 vertex");
        assertEquals(0, Graph6Codec.decodeGraph6("@").edges().size());
        assertEquals(2, Graph6Codec.decodeGraph6("A?").n(), "'A?' is 2 vertices, no edges");
        assertEquals(0, Graph6Codec.decodeGraph6("A?").edges().size());

        Graph6Codec.Graph oneEdge = Graph6Codec.decodeGraph6("A_");
        assertEquals(2, oneEdge.n());
        assertEquals(List.of(new Graph6Codec.Edge(0, 1)), oneEdge.edges());
    }

    @Test
    void graph6EmptyGraph() {
        // n=0: N(0)="?" and no data bytes.
        Graph6Codec.Graph g = Graph6Codec.decodeGraph6("?");
        assertEquals(0, g.n());
        assertEquals(0, g.edges().size());
        assertEquals("?", Graph6Codec.encodeGraph6(0, List.of()));
    }

    @Test
    void graph6IgnoresSelfLoopsOnEncode() {
        // A self-loop is not representable; it must simply be dropped, leaving the empty graph on 3 vertices.
        String encoded = Graph6Codec.encodeGraph6(3, List.of(new Graph6Codec.Edge(1, 1)));
        assertEquals(0, Graph6Codec.decodeGraph6(encoded).edges().size());
        assertEquals(3, Graph6Codec.decodeGraph6(encoded).n());
    }

    @Test
    void graph6RoundTripsAllSampleStrings() {
        for (String s : new String[]{"DQc", "DF{", "D]w", "@", "A?", "A_", "BW", "E?Bw", "F?B@o"}) {
            Graph6Codec.Graph g = Graph6Codec.decodeGraph6(s);
            assertEquals(s, Graph6Codec.encodeGraph6(g.n(), g.edges()), "graph6 round-trip for '" + s + "'");
        }
    }

    @Test
    void graph6LargeVertexCount() {
        // n = 100 (uses the 4-byte N(n) form) with a single edge 0-99.
        String encoded = Graph6Codec.encodeGraph6(100, List.of(new Graph6Codec.Edge(0, 99)));
        Graph6Codec.Graph g = Graph6Codec.decodeGraph6(encoded);
        assertEquals(100, g.n());
        assertEquals(List.of(new Graph6Codec.Edge(0, 99)), g.edges());
    }

    // ---------------------------------------------------------------- sparse6

    @Test
    void sparse6SpecWorkedExample() {
        // formats.txt: ":Fa@x^" is n=7 with edges 0-1, 0-2, 1-2, 5-6 (trailing padding must not add an edge).
        Graph6Codec.Graph g = Graph6Codec.decodeSparse6(":Fa@x^");
        assertEquals(7, g.n());
        assertEquals(List.of(
                new Graph6Codec.Edge(0, 1),
                new Graph6Codec.Edge(0, 2),
                new Graph6Codec.Edge(1, 2),
                new Graph6Codec.Edge(5, 6)
        ), g.edges());
    }

    // ---------------------------------------------------------------- digraph6

    @Test
    void digraph6SpecWorkedExample() {
        // formats.txt: n=5, directed edges {0->2, 0->4, 3->1, 3->4}.
        Graph6Codec.Graph g = Graph6Codec.decodeDigraph6("&DI?AO");
        assertEquals(5, g.n());
        assertEquals(List.of(
                new Graph6Codec.Edge(0, 2),
                new Graph6Codec.Edge(0, 4),
                new Graph6Codec.Edge(3, 1),
                new Graph6Codec.Edge(3, 4)
        ), g.edges());
    }

    @Test
    void digraph6RoundTripDirectedEdges() {
        List<Graph6Codec.Edge> edges = List.of(
                new Graph6Codec.Edge(0, 2),
                new Graph6Codec.Edge(0, 4),
                new Graph6Codec.Edge(3, 1),
                new Graph6Codec.Edge(3, 4)
        );
        String encoded = Graph6Codec.encodeDigraph6(5, edges);
        Graph6Codec.Graph g = Graph6Codec.decodeDigraph6(encoded);
        assertEquals(5, g.n());
        assertEquals(edges, g.edges(), "directed edges (and their direction) survive digraph6 round-trip");
    }

    @Test
    void digraph6SupportsSelfLoops() {
        List<Graph6Codec.Edge> edges = List.of(new Graph6Codec.Edge(2, 2));
        String encoded = Graph6Codec.encodeDigraph6(3, edges);
        assertEquals(edges, Graph6Codec.decodeDigraph6(encoded).edges(), "digraph6 keeps self-loops");
    }
}
