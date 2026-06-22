package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes a CJ document as one or more {@link Graph6Reader graph6} lines (one line per CJ graph, newline
 * separated). graph6 is simple, undirected and unlabelled, so labels, data, edge direction and ports are
 * dropped, and self-loops are not representable; only the vertex set and the undirected edge set survive.
 * <p>
 * Vertices are mapped to positional indices {@code 0..n-1}. When all node ids are non-negative integers they
 * keep their numeric value as the index (so the {@link Graph6Reader} output round-trips exactly); otherwise
 * ids are numbered in encounter order.
 */
public class Graph6Writer implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                writeCjDocument(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return Graph6Reader.FORMAT;
    }

    @Override
    public void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        StringBuilder out = new StringBuilder();
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (ICjGraph graph : graphs) {
            out.append(encodeGraph(graph)).append('\n');
        }
        outputSink.write(out.toString());
    }

    private String encodeGraph(ICjGraph graph) {
        // Build a stable index for every node id (positional 0..n-1). Honour numeric "0".."n-1" ids by value
        // so reader output round-trips; fall back to encounter order for arbitrary ids.
        Map<String, Integer> indexById = assignIndices(graph);
        int n = indexById.size();

        List<Graph6Codec.Edge> edges = new ArrayList<>();
        graph.edges().forEach(e -> {
            List<ICjEndpoint> eps = e.endpoints().toList();
            if (eps.size() != 2) {
                return; // graph6 cannot represent hyper-edges
            }
            Integer a = indexById.get(eps.get(0).node());
            Integer b = indexById.get(eps.get(1).node());
            if (a == null || b == null || a.equals(b)) {
                return; // unknown endpoint or self-loop (not representable in graph6)
            }
            edges.add(new Graph6Codec.Edge(a, b));
        });
        return Graph6Codec.encodeGraph6(n, edges);
    }

    /**
     * Maps each node id to a 0-based index. If every id parses as a non-negative integer within {@code 0..n-1}
     * with no duplicates, that integer is used directly; otherwise ids are numbered in document order.
     */
    private Map<String, Integer> assignIndices(ICjGraph graph) {
        List<String> ids = new ArrayList<>();
        graph.nodes().map(ICjNode::id).filter(id -> id != null).forEach(ids::add);

        Map<String, Integer> numeric = new LinkedHashMap<>();
        boolean canUseNumeric = true;
        for (String id : ids) {
            try {
                int v = Integer.parseInt(id);
                if (v < 0 || v >= ids.size() || numeric.containsValue(v)) {
                    canUseNumeric = false;
                    break;
                }
                numeric.put(id, v);
            } catch (NumberFormatException e) {
                canUseNumeric = false;
                break;
            }
        }
        if (canUseNumeric && numeric.size() == ids.size()) {
            return numeric;
        }

        Map<String, Integer> byOrder = new LinkedHashMap<>();
        for (String id : ids) {
            byOrder.putIfAbsent(id, byOrder.size());
        }
        return byOrder;
    }
}
