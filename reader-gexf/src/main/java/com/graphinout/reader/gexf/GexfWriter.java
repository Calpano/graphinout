package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writes an {@link ICjDocument} as a GEXF 1.3 document (the inverse of {@link GexfReader}). Emits one {@code <node>}
 * per node (with {@code label} and {@code <attvalues>} when present) and one {@code <edge>} per edge, using
 * {@code defaultedgetype="directed"} but always emitting an explicit per-edge {@code type} so undirected and mixed
 * directionality survive. Edge {@code type()} is carried via the {@code kind} attribute (GEXF 1.3), graphs nested in
 * nodes are emitted as a GEXF hierarchy (a {@code <nodes>}/{@code <edges>} block inside the {@code <node>}), and
 * graph-level data is emitted as an {@code <attvalues>} block directly under {@code <graph>}.
 */
public class GexfWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                write(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    private void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        outputSink.write(toGexf(cjDoc));
    }

    @Override
    public GioFileFormat fileFormat() {
        return GexfReader.FORMAT;
    }

    public static String toGexf(ICjDocument cjDoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gexf xmlns=\"http://www.gexf.net/1.3\" version=\"1.3\">\n");

        // GEXF is one-graph-per-file: emit the first top-level graph (others, if any, are merged in below).
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        AtomicInteger edgeId = new AtomicInteger();
        sb.append("  <graph defaultedgetype=\"directed\">\n");
        if (!graphs.isEmpty()) {
            // graph-level attributes: emit before nodes so the reader can attach them while still on <graph>
            appendAttvalues(sb, "    ", graphs.getFirst().data());
        }
        sb.append("    <nodes>\n");
        for (ICjGraph g : graphs) {
            g.nodes().forEach(n -> appendNode(sb, "      ", n));
        }
        sb.append("    </nodes>\n");
        sb.append("    <edges>\n");
        for (ICjGraph g : graphs) {
            g.edges().forEach(e -> appendEdge(sb, "      ", e, edgeId));
        }
        sb.append("    </edges>\n");
        sb.append("  </graph>\n");
        sb.append("</gexf>\n");
        return sb.toString();
    }

    private static void appendNode(StringBuilder sb, String indent, ICjNode n) {
        String label = firstLabel(n.labelEntries());
        List<ICjGraph> nested = n.graphs().toList();
        boolean hasBody = !nested.isEmpty() || !n.data().isEmpty();

        sb.append(indent).append("<node id=\"").append(esc(n.id())).append('"');
        if (label != null) {
            sb.append(" label=\"").append(esc(label)).append('"');
        }
        if (!hasBody) {
            sb.append("/>\n");
            return;
        }
        sb.append(">\n");
        appendAttvalues(sb, indent + "  ", n.data());
        // GEXF hierarchy: nested graphs become child <nodes>/<edges> directly under the <node>.
        if (!nested.isEmpty()) {
            AtomicInteger edgeId = new AtomicInteger();
            sb.append(indent).append("  <nodes>\n");
            for (ICjGraph sub : nested) {
                sub.nodes().forEach(child -> appendNode(sb, indent + "    ", child));
            }
            sb.append(indent).append("  </nodes>\n");
            boolean anyEdges = nested.stream().anyMatch(sub -> sub.edges().findAny().isPresent());
            if (anyEdges) {
                sb.append(indent).append("  <edges>\n");
                for (ICjGraph sub : nested) {
                    sub.edges().forEach(e -> appendEdge(sb, indent + "    ", e, edgeId));
                }
                sb.append(indent).append("  </edges>\n");
            }
        }
        sb.append(indent).append("</node>\n");
    }

    private static void appendEdge(StringBuilder sb, String indent, ICjEdge e, AtomicInteger edgeId) {
        String[] st = sourceTarget(e.endpoints().toList());
        if (st == null) {
            return; // GEXF edges are binary; skip hyper-edges
        }
        boolean directed = isDirected(e.endpoints().toList());
        sb.append(indent).append("<edge id=\"").append(edgeId.getAndIncrement()).append('"')
          .append(" source=\"").append(esc(st[0])).append('"')
          .append(" target=\"").append(esc(st[1])).append('"')
          .append(" type=\"").append(directed ? "directed" : "undirected").append('"');
        String type = e.type();
        if (type != null && !type.isBlank()) {
            sb.append(" kind=\"").append(esc(type)).append('"');
        }
        String label = firstLabel(e.labelEntries());
        if (label != null) {
            sb.append(" label=\"").append(esc(label)).append('"');
        }
        if (e.data().isEmpty()) {
            sb.append("/>\n");
        } else {
            sb.append(">\n");
            appendAttvalues(sb, indent + "  ", e.data());
            sb.append(indent).append("</edge>\n");
        }
    }

    /** Emit an {@code <attvalues>} block for the object-typed data, one {@code <attvalue>} per top-level property. */
    private static void appendAttvalues(StringBuilder sb, String indent, ICjData data) {
        IJsonValue json = data.jsonValue();
        if (json == null || !json.isObject()) {
            return;
        }
        StringBuilder body = new StringBuilder();
        json.asObject().forEach((key, value) -> {
            if (value == null || value.isNull()) {
                return;
            }
            String text = value.isString() ? value.asString() : value.toJsonString();
            body.append(indent).append("  <attvalue for=\"").append(esc(key))
                .append("\" value=\"").append(esc(text)).append("\"/>\n");
        });
        if (body.length() == 0) {
            return;
        }
        sb.append(indent).append("<attvalues>\n").append(body).append(indent).append("</attvalues>\n");
    }

    private static @Nullable String firstLabel(List<ICjLabelEntry> labels) {
        if (labels.isEmpty()) {
            return null;
        }
        String value = labels.getFirst().value();
        return (value == null || value.isEmpty()) ? null : value;
    }

    /** An edge is undirected only if every endpoint is undirected; otherwise treat it as directed. */
    private static boolean isDirected(List<ICjEndpoint> endpoints) {
        return endpoints.stream().anyMatch(ICjEndpoint::isDirected);
    }

    private static String @Nullable [] sourceTarget(List<ICjEndpoint> endpoints) {
        ICjEndpoint in = null, out = null;
        for (ICjEndpoint ep : endpoints) {
            if (ep.direction() == CjDirection.IN && in == null) in = ep;
            else if (ep.direction() == CjDirection.OUT && out == null) out = ep;
        }
        if (in != null && out != null) {
            return new String[]{in.node(), out.node()};
        }
        if (endpoints.size() == 2) {
            return new String[]{endpoints.get(0).node(), endpoints.get(1).node()};
        }
        return null;
    }

    private static String esc(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> b.append("&amp;");
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                case '"' -> b.append("&quot;");
                case '\'' -> b.append("&apos;");
                default -> b.append(c);
            }
        }
        return b.toString();
    }

}
