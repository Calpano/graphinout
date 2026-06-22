package com.graphinout.reader.jgrapht;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure codec for the nauty graph6 / sparse6 / digraph6 ASCII graph formats.
 * <p>
 * Spec: <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">B. McKay, graph formats (nauty)</a>.
 * <p>
 * All three formats share two building blocks:
 * <ul>
 *   <li>{@code N(n)} encodes the vertex count {@code n} as 1, 4 or 8 bytes.</li>
 *   <li>{@code R(x)} ASCII-armors a bit vector: pad on the right with 0-bits to a multiple of 6, split into
 *       6-bit big-endian groups, add 63 to each group (yielding printable bytes 63..126).</li>
 * </ul>
 * graph6 stores the upper triangle of the (symmetric, loop-free) adjacency matrix column by column; digraph6
 * stores the full {@code n*n} matrix row by row; sparse6 stores an increasing edge list. This codec works on
 * {@code String} (one ASCII byte per char, which is exactly how these formats are defined) and uses adjacency
 * bit-sets so it never instantiates a dense matrix for sparse inputs.
 */
final class Graph6Codec {

    static final String HEADER_GRAPH6 = ">>graph6<<";
    static final String HEADER_SPARSE6 = ">>sparse6<<";
    static final String HEADER_DIGRAPH6 = ">>digraph6<<";

    private Graph6Codec() {
    }

    /** An undirected (graph6/sparse6) or directed (digraph6) edge between positional vertices. */
    record Edge(int from, int to) {
    }

    /** The decoded content of a single graph6/sparse6/digraph6 line. */
    record Graph(int n, List<Edge> edges) {
    }

    // ------------------------------------------------------------------ N(n)

    /** Encodes the vertex count as {@code N(n)}. */
    static void appendN(StringBuilder out, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("graph6: negative vertex count " + n);
        }
        if (n <= 62) {
            out.append((char) (n + 63));
        } else if (n <= 258047) {
            out.append((char) 126);
            appendR(out, intToBits(n, 18));
        } else if (n <= 68719476735L) {
            out.append((char) 126).append((char) 126);
            appendR(out, longToBits(n, 36));
        } else {
            throw new IllegalArgumentException("graph6: vertex count too large: " + n);
        }
    }

    /**
     * Reads {@code N(n)} starting at {@code cursor.pos} and advances the cursor past it.
     *
     * @return the decoded vertex count
     */
    static int readN(String s, Cursor cursor) {
        int first = byteAt(s, cursor.pos);
        if (first != 126) {
            cursor.pos += 1;
            return first - 63;
        }
        int second = byteAt(s, cursor.pos + 1);
        if (second != 126) {
            // 126 followed by 3 bytes => 18-bit value
            long v = readBigEndian(s, cursor.pos + 1, 3);
            cursor.pos += 4;
            return (int) v;
        }
        // 126 126 followed by 6 bytes => 36-bit value
        long v = readBigEndian(s, cursor.pos + 2, 6);
        cursor.pos += 8;
        return (int) v;
    }

    // ------------------------------------------------------------------ R(x)

    /** ASCII-armors {@code bits} as {@code R(x)}: pad to a multiple of 6, 6-bit big-endian groups, each +63. */
    static void appendR(StringBuilder out, List<Boolean> bits) {
        int padded = ((bits.size() + 5) / 6) * 6;
        for (int i = 0; i < padded; i += 6) {
            int group = 0;
            for (int j = 0; j < 6; j++) {
                group <<= 1;
                int idx = i + j;
                if (idx < bits.size() && bits.get(idx)) {
                    group |= 1;
                }
            }
            out.append((char) (group + 63));
        }
    }

    /** Decodes the {@code R(x)} bytes from {@code from} to end-of-string into a bit list (6 bits per byte). */
    static List<Boolean> readBits(String s, int from) {
        List<Boolean> bits = new ArrayList<>((s.length() - from) * 6);
        for (int i = from; i < s.length(); i++) {
            int value = byteAt(s, i) - 63;
            if (value < 0 || value > 63) {
                throw new IllegalArgumentException("graph6: invalid data byte " + byteAt(s, i) + " at position " + i);
            }
            for (int j = 5; j >= 0; j--) {
                bits.add((value & (1 << j)) != 0);
            }
        }
        return bits;
    }

    // ------------------------------------------------------------------ graph6

    /** Encodes a simple undirected graph as one graph6 line (no header, no newline). */
    static String encodeGraph6(int n, List<Edge> edges) {
        boolean[][] adj = adjacencyMatrix(n, edges, false);
        StringBuilder out = new StringBuilder();
        appendN(out, n);
        // upper triangle, column by column: (0,1),(0,2),(1,2),(0,3),(1,3),(2,3),...
        List<Boolean> bits = new ArrayList<>(n * (n - 1) / 2);
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < j; i++) {
                bits.add(adj[i][j]);
            }
        }
        appendR(out, bits);
        return out.toString();
    }

    /** Decodes one graph6 line (header already stripped). */
    static Graph decodeGraph6(String line) {
        Cursor cursor = new Cursor();
        int n = readN(line, cursor);
        List<Boolean> bits = readBits(line, cursor.pos);
        List<Edge> edges = new ArrayList<>();
        int idx = 0;
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < j; i++) {
                if (idx < bits.size() && bits.get(idx)) {
                    edges.add(new Edge(i, j));
                }
                idx++;
            }
        }
        return new Graph(n, edges);
    }

    // ------------------------------------------------------------------ digraph6

    /** Encodes a directed graph (self-loops allowed) as one digraph6 line ({@code &} prefix, no newline). */
    static String encodeDigraph6(int n, List<Edge> edges) {
        boolean[][] adj = adjacencyMatrix(n, edges, true);
        StringBuilder out = new StringBuilder();
        out.append('&');
        appendN(out, n);
        // full n*n matrix, row by row
        List<Boolean> bits = new ArrayList<>(n * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                bits.add(adj[i][j]);
            }
        }
        appendR(out, bits);
        return out.toString();
    }

    /** Decodes one digraph6 line including its leading {@code &}. */
    static Graph decodeDigraph6(String line) {
        if (line.isEmpty() || line.charAt(0) != '&') {
            throw new IllegalArgumentException("digraph6: line must start with '&'");
        }
        Cursor cursor = new Cursor();
        cursor.pos = 1;
        int n = readN(line, cursor);
        List<Boolean> bits = readBits(line, cursor.pos);
        List<Edge> edges = new ArrayList<>();
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (idx < bits.size() && bits.get(idx)) {
                    edges.add(new Edge(i, j));
                }
                idx++;
            }
        }
        return new Graph(n, edges);
    }

    // ------------------------------------------------------------------ sparse6

    /** Decodes one sparse6 line including its leading {@code :}. */
    static Graph decodeSparse6(String line) {
        if (line.isEmpty() || line.charAt(0) != ':') {
            throw new IllegalArgumentException("sparse6: line must start with ':'");
        }
        Cursor cursor = new Cursor();
        cursor.pos = 1;
        int n = readN(line, cursor);
        List<Boolean> bits = readBits(line, cursor.pos);

        // k = number of bits to represent n-1
        int k = bitsToRepresent(n - 1);
        List<Edge> edges = new ArrayList<>();
        int pos = 0;
        int v = 0;
        // Each step reads a 1-bit flag b followed by a k-bit vertex index x.
        while (pos + 1 + k <= bits.size()) {
            boolean b = bits.get(pos);
            pos++;
            int x = 0;
            for (int j = 0; j < k; j++) {
                x = (x << 1) | (bits.get(pos) ? 1 : 0);
                pos++;
            }
            if (b) {
                v++;
            }
            // Tuples with x >= n or v >= n can only arise from the trailing 1-bit padding (the spec pads
            // incomplete final tuples with 1-bits, which inflates b/x). Such phantom tuples are discarded and
            // nothing valid follows them, so we stop. See nauty formats.txt / networkx from_sparse6_bytes.
            if (x >= n || v >= n) {
                break;
            }
            if (x > v) {
                v = x;
            } else {
                edges.add(new Edge(x, v));
            }
        }
        return new Graph(n, edges);
    }

    // ------------------------------------------------------------------ helpers

    /** A simple mutable index into a string, used to thread position through {@code N(n)} reads. */
    static final class Cursor {
        int pos = 0;
    }

    /**
     * Builds an adjacency matrix; for the undirected case ({@code directed == false}) self-loops are dropped
     * (graph6 cannot represent them) and both {@code [i][j]} and {@code [j][i]} are set.
     */
    private static boolean[][] adjacencyMatrix(int n, List<Edge> edges, boolean directed) {
        boolean[][] adj = new boolean[n][n];
        for (Edge e : edges) {
            int a = e.from();
            int b = e.to();
            if (a < 0 || a >= n || b < 0 || b >= n) {
                throw new IllegalArgumentException("edge endpoint out of range 0.." + (n - 1) + ": " + e);
            }
            if (directed) {
                adj[a][b] = true;
            } else {
                if (a == b) {
                    continue; // graph6 has no self-loops
                }
                adj[a][b] = true;
                adj[b][a] = true;
            }
        }
        return adj;
    }

    /** Number of bits needed to represent {@code value} (0 needs 1 bit; sparse6 uses this for n-1). */
    private static int bitsToRepresent(int value) {
        if (value <= 0) {
            return 1;
        }
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    private static List<Boolean> intToBits(int value, int width) {
        return longToBits(value, width);
    }

    private static List<Boolean> longToBits(long value, int width) {
        List<Boolean> bits = new ArrayList<>(width);
        for (int j = width - 1; j >= 0; j--) {
            bits.add(((value >> j) & 1L) != 0);
        }
        return bits;
    }

    private static long readBigEndian(String s, int from, int byteCount) {
        long v = 0;
        for (int i = 0; i < byteCount; i++) {
            int group = byteAt(s, from + i) - 63;
            if (group < 0 || group > 63) {
                throw new IllegalArgumentException("graph6: invalid N(n) byte at position " + (from + i));
            }
            v = (v << 6) | group;
        }
        return v;
    }

    private static int byteAt(String s, int pos) {
        if (pos >= s.length()) {
            throw new IllegalArgumentException("graph6: unexpected end of data at position " + pos);
        }
        return s.charAt(pos);
    }
}
