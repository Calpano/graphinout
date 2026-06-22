package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Shared decode-and-emit logic for the three nauty formats. Each non-empty, non-header input line is one graph
 * (graph6/sparse6/digraph6 files routinely contain many graphs, one per line). Every line becomes one CJ graph
 * with positional node ids {@code "0".."n-1"}; when a file holds more than one graph the graphs get ids
 * {@code "g0","g1",...} so the resulting CJ document is well-formed.
 */
final class Graph6Emitter {

    private Graph6Emitter() {
    }

    /**
     * @param directed whether edges are emitted as directed (digraph6) or undirected (graph6/sparse6)
     * @param decoder  turns one already-trimmed line into a decoded {@link Graph6Codec.Graph}
     */
    static void read(InputSource inputSource, ICjStream cjStream, boolean directed,
                     Function<String, Graph6Codec.Graph> decoder) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = sis.getContentAsUtf8String();

        List<String> lines = splitLines(content);
        cjStream.documentStart(cjStream.createDocumentChunk());
        boolean multi = lines.size() > 1;
        int graphIndex = 0;
        for (String line : lines) {
            Graph6Codec.Graph graph;
            try {
                graph = decoder.apply(line);
            } catch (RuntimeException e) {
                cjStream.sendContentError_Error("Invalid graph6 line: " + e.getMessage(), e, null);
                continue;
            }
            emitGraph(cjStream, graph, directed, multi ? "g" + graphIndex : null);
            graphIndex++;
        }
        cjStream.documentEnd();
    }

    private static void emitGraph(ICjStream cjStream, Graph6Codec.Graph graph, boolean directed, String graphId) {
        ICjGraphChunkMutable graphChunk = cjStream.createGraphChunk();
        if (graphId != null) {
            graphChunk.id(graphId);
        }
        cjStream.graphStart(graphChunk);

        for (int i = 0; i < graph.n(); i++) {
            ICjNodeChunkMutable node = cjStream.createNodeChunk();
            node.id(Integer.toString(i));
            cjStream.node(node);
        }
        for (Graph6Codec.Edge edge : graph.edges()) {
            ICjEdgeChunkMutable edgeChunk = cjStream.createEdgeChunk();
            if (directed) {
                edgeChunk.addEndpointOutgoing(Integer.toString(edge.from()));
                edgeChunk.addEndpointIncoming(Integer.toString(edge.to()));
            } else {
                edgeChunk.addEndpointUndirected(Integer.toString(edge.from()));
                edgeChunk.addEndpointUndirected(Integer.toString(edge.to()));
            }
            cjStream.edge(edgeChunk);
        }
        cjStream.graphEnd();
    }

    /**
     * Splits into payload lines, dropping blank lines and the optional {@code >>graph6<<} / {@code >>sparse6<<}
     * / {@code >>digraph6<<} header (which may appear standalone or as a prefix on the first data line).
     */
    private static List<String> splitLines(String content) {
        return java.util.Arrays.stream(content.split("\r\n|\r|\n"))
                .map(String::trim)
                .map(Graph6Emitter::stripHeader)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String stripHeader(String line) {
        for (String header : new String[]{Graph6Codec.HEADER_GRAPH6, Graph6Codec.HEADER_SPARSE6, Graph6Codec.HEADER_DIGRAPH6}) {
            if (line.startsWith(header)) {
                return line.substring(header.length()).trim();
            }
        }
        return line;
    }
}
