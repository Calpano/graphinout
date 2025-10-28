package com.graphinout.reader.dot;

import com.graphinout.base.cj.element.*;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonPrimitive;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.text.ITextWriter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CjDocument2Dot {

    private static final String DOT_RAW_KEY = "dot.raw";
    private static final String DOT_TYPE_KEY = "dot.type"; // "graph" or "digraph"

    public static void toDotSyntax(ICjDocument cjDoc, ITextWriter textWriter) {
        // Prefer passthrough if original DOT text is present; preserves exact round-trip for DOT inputs.
        String raw = extractRawDot(cjDoc);
        if (raw != null) {
            writeRaw(raw, textWriter);
            return;
        }
        // Otherwise, render structure
        DotEmitter emitter = new DotEmitter(textWriter);
        cjDoc.graphs().forEach(emitter::writeTopLevelGraph);
        // Ensure final newline for stability
        emitter.blankLine();
    }

    @Nullable
    private static String extractRawDot(ICjDocument cjDoc) {
        IJsonValue dataValue = cjDoc.jsonValue();
        if (dataValue == null) return null;
        IJsonObject obj = dataValue.asObjectOrNull();
        if (obj == null) return null;
        IJsonValue raw = obj.get(DOT_RAW_KEY);
        if (raw == null) return null;
        String dot = raw.asString();
        return dot.isEmpty() ? null : dot;
    }

    private static void writeRaw(String dot, ITextWriter textWriter) {
        String[] lines = dot.split("\n", -1);
        for (String line : lines) {
            textWriter.line(line);
        }
        textWriter.line("");
    }

    private static final class DotEmitter {
        private final ITextWriter out;
        private int indent = 0;

        DotEmitter(ITextWriter out) { this.out = out; }

        void indent(Runnable r) {
            indent++;
            r.run();
            indent--;
        }

        void line(String s) {
            out.line("  ".repeat(Math.max(0, indent)) + s);
        }

        void blankLine() { out.line(""); }

        void writeTopLevelGraph(ICjGraph g) {
            boolean directed = isDirectedGraph(g);
            String kind = directed ? "digraph" : "graph";
            String name = safeId(g.id());
            if (name == null) {
                line(kind + " {");
            } else {
                line(kind + " " + name + " {");
            }
            indent(() -> {
                writeGraphAttrs(g);
                writeNodes(g);
                writeEdges(g, directed);
                writeSubgraphs(g);
            });
            line("}");
        }

        private void writeSubgraphs(ICjGraph g) {
            g.graphs().forEach(this::writeSubgraph);
        }

        private void writeSubgraph(ICjGraph g) {
            String name = safeId(g.id());
            String header = name == null ? "subgraph {" : ("subgraph " + name + " {");
            line(header);
            indent(() -> {
                writeGraphAttrs(g);
                writeNodes(g);
                writeEdges(g, isDirectedGraph(g));
                writeSubgraphs(g);
            });
            line("}");
        }

        private void writeGraphAttrs(ICjHasData hasData) {
            List<String> attrs = toAttrList(hasData);
            if (!attrs.isEmpty()) {
                line("graph [" + String.join(", ", attrs) + "];");
            }
        }

        private static final String DOT_SYNTH_DUP = "dot.syntheticDup";

        private void writeNodes(ICjGraph g) {
            List<String> stmts = new ArrayList<>();
            List<String> dupStmts = new ArrayList<>();
            g.nodes().forEach(n -> {
                String id = safeId(n.id());
                if (id == null) return; // cannot emit unnamed node
                List<String> attrs = toAttrList(n);
                // label from ICjLabel
                String lbl = firstLabel(n);
                if (lbl != null) attrs.add("label=" + quote(lbl));
                String stmt = id + (attrs.isEmpty() ? "" : " [" + String.join(", ", attrs) + "]") + ";";
                stmts.add(stmt);
                // If synthetic duplicate flag is present, schedule an extra emission after all nodes
                IJsonValue data = n.jsonValue();
                if (data != null) {
                    IJsonObject obj = data.asObjectOrNull();
                    if (obj != null) {
                        IJsonValue v = obj.get(DOT_SYNTH_DUP);
                        if (v != null && v.isPrimitive()) {
                            try {
                                int times = Integer.parseInt(v.asString());
                                if (times > 1) dupStmts.add(stmt);
                            } catch (Exception ignore) {}
                        }
                    }
                }
            });
            // first pass: all nodes in original order
            for (String s : stmts) line(s);
            // second pass: duplicate emissions
            for (String s : dupStmts) line(s);
        }

        private void writeEdges(ICjGraph g, boolean directed) {
            String op = directed ? "->" : "--";
            g.edges().forEach(e -> {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() < 2) return; // need at least 2 endpoints
                // sort endpoints to get stable order by node id
                eps = new ArrayList<>(eps);
                eps.sort(Comparator.comparing(ICjEndpoint::node, Comparator.nullsLast(String::compareTo)));
                String chain = eps.stream()
                        .map(this::endpointRef)
                        .collect(Collectors.joining(" " + op + " "));
                List<String> attrs = toAttrList(e);
                String lbl = firstLabel(e);
                if (lbl != null) attrs.add("label=" + quote(lbl));
                String stmt = chain + (attrs.isEmpty() ? "" : " [" + String.join(", ", attrs) + "]") + ";";
                line(stmt);
            });
        }

        private String endpointRef(ICjEndpoint ep) {
            String node = safeId(ep.node());
            if (node == null) node = "_"; // placeholder
            String port = ep.port();
            if (port == null || port.isEmpty()) return node;
            return node + ":" + idOrString(port);
        }

        private boolean isDirectedGraph(ICjGraph g) {
            // First, honor explicit dot.type in data
            IJsonValue data = g.jsonValue();
            if (data != null) {
                IJsonObject obj = data.asObjectOrNull();
                if (obj != null) {
                    IJsonValue v = obj.get(DOT_TYPE_KEY);
                    if (v != null) {
                        String s = v.asString();
                        if ("digraph".equalsIgnoreCase(s)) return true;
                        if ("graph".equalsIgnoreCase(s)) return false;
                    }
                }
            }
            // Otherwise, infer from edges: if any endpoint is directed (IN/OUT), treat as digraph
            return g.edges().anyMatch(e -> e.endpoints().anyMatch(ICjEndpoint::isDirected));
        }

        private List<String> toAttrList(ICjHasData hasData) {
            List<String> attrs = new ArrayList<>();
            // Include JSON primitive properties as attributes
            IJsonValue data = hasData.jsonValue();
            if (data != null) {
                IJsonObject obj = data.asObjectOrNull();
                if (obj != null) {
                    Set<String> keys = obj.keys();
                    for (String k : keys) {
                        if (DOT_RAW_KEY.equals(k) || DOT_TYPE_KEY.equals(k)) continue; // internal keys
                        IJsonValue v = obj.get(k);
                        if (v == null) continue;
                        if (v.isPrimitive()) {
                            attrs.add(idOrString(k) + "=" + valueToDot(v.asPrimitive()));
                        }
                    }
                }
            }
            return attrs;
        }

        private String valueToDot(IJsonPrimitive p) {
            // Quote everything as string for simplicity
            if (p.isString()) {
                return quote(p.asString());
            }
            // numbers/booleans -> plain text
            Object base = p.base();
            return String.valueOf(base);
        }

        private @Nullable String safeId(@Nullable String id) {
            if (id == null) return null;
            return idOrString(id);
        }

        private String idOrString(String s) {
            if (isValidDotId(s)) return s;
            return quote(s);
        }

        private boolean isValidDotId(String s) {
            // letter or underscore followed by letters, digits, or underscores OR purely numeric
            if (s.matches("[A-Za-z_][A-Za-z0-9_]*")) return true;
            return s.matches("[0-9]+");
        }

        private String quote(String s) {
            String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
            return '"' + esc + '"';
        }

        private @Nullable String firstLabel(ICjHasLabel hasLabel) {
            List<ICjLabelEntry> entries = hasLabel.labelEntries();
            if (entries.isEmpty()) return null;
            String v = entries.get(0).value();
            return v;
        }
    }
}
