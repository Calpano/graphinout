package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.*;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.base.text.ITextWriter;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CjDocument2Dot {

    private static final String DOT_RAW_KEY = "dot.raw";
    private static final String DOT_TYPE_KEY = "dot.type"; // "graph" or "digraph"

    /** Prefix marking a DOT subgraph that holds a node's nested graph (nested-graphs-in-nodes). */
    public static final String NODE_CLUSTER_PREFIX = "cluster_nodegraph__";
    /** Separator between owning-node id and inner-graph id within a node-cluster subgraph name. */
    public static final String NODE_CLUSTER_SEP = "__graph__";

    public static void toDotSyntax(ICjDocument cjDoc, ITextWriter textWriter) {
        // Prefer passthrough if original DOT text is present; preserves exact round-trip for DOT inputs.
        String raw = extractRawDot(cjDoc);
        if (raw != null) {
            writeRaw(raw, textWriter);
            return;
        }
        // Otherwise, render structure
        DotEmitter emitter = new DotEmitter(textWriter, nodesById(cjDoc));
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

    /** All nodes by id, document-wide: DOT node ids are global, and an edge may reference a node in another graph. */
    private static Map<String, ICjNode> nodesById(ICjDocument cjDoc) {
        Map<String, ICjNode> map = new HashMap<>();
        cjDoc.graphs().forEach(g -> collectNodes(g, map));
        return map;
    }

    private static void collectNodes(ICjGraph g, Map<String, ICjNode> map) {
        g.nodes().forEach(n -> {
            if (n.id() != null) map.putIfAbsent(n.id(), n);
            n.graphs().forEach(inner -> collectNodes(inner, map));
        });
        g.graphs().forEach(sub -> collectNodes(sub, map));
    }

    private static @Nullable String dataString(ICjHasData hasData, String key) {
        IJsonValue data = hasData.jsonValue();
        IJsonObject obj = data == null ? null : data.asObjectOrNull();
        IJsonValue v = obj == null ? null : obj.get(key);
        return v != null && v.isPrimitive() ? v.asString() : null;
    }

    private static boolean dataBoolean(ICjHasData hasData, String key) {
        IJsonValue data = hasData.jsonValue();
        IJsonObject obj = data == null ? null : data.asObjectOrNull();
        IJsonValue v = obj == null ? null : obj.get(key);
        if (v == null || !v.isPrimitive()) return false;
        Object base = v.asPrimitive().base();
        return Boolean.TRUE.equals(base) || "true".equals(base);
    }

    /** Order endpoints for a directed DOT edge: source (IN) first, undirected middle, target (OUT) last. */
    private static int directedRank(ICjEndpoint ep) {
        if (ep.direction() == CjDirection.IN) return 0;
        if (ep.direction() == CjDirection.OUT) return 2;
        return 1;
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
        private final Map<String, ICjNode> nodesById;
        private int indent = 0;

        DotEmitter(ITextWriter out, Map<String, ICjNode> nodesById) {
            this.out = out;
            this.nodesById = nodesById;
        }

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
                ICjLabelEntry labelEntry = firstLabelEntry(n);
                String lbl = labelEntry == null ? null : labelEntry.value();
                boolean html = lbl != null && isHtmlLabel(labelEntry);
                String labelDot = lbl == null ? null : html ? lbl : quote(lbl);
                // Graphviz only knows ports a record or HTML label declares; make the node declare its ports
                List<String> declared = declaredPorts(n);
                if (!declared.isEmpty() && !html) {
                    if (!DotPorts.isRecordShape(dataString(n, "shape"))) {
                        attrs.removeIf(a -> a.startsWith("shape="));
                        attrs.add("shape=record");
                    }
                    if (lbl == null || !DotPorts.recordPorts(lbl).containsAll(declared)) {
                        labelDot = recordLabel(lbl, declared);
                    }
                }
                if (labelDot != null) attrs.add("label=" + labelDot);
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
            // third pass: nested graphs owned by a node -> emit as DOT clusters (registry: "Via subgraphs/clusters").
            // The cluster name encodes the owning node id and the inner graph id so the reader can reattach it to the
            // node (nested-graphs-in-nodes), rather than to the parent graph.
            g.nodes().forEach(n -> {
                String nodeId = n.id();
                if (nodeId == null) return;
                n.graphs().forEach(inner -> writeNodeCluster(nodeId, inner));
            });
        }

        private void writeNodeCluster(String nodeId, ICjGraph inner) {
            String innerId = inner.id() == null ? "" : inner.id();
            String clusterName = NODE_CLUSTER_PREFIX + nodeId + NODE_CLUSTER_SEP + innerId;
            line("subgraph " + idOrString(clusterName) + " {");
            indent(() -> {
                writeGraphAttrs(inner);
                writeNodes(inner);
                writeEdges(inner, isDirectedGraph(inner));
                writeSubgraphs(inner);
            });
            line("}");
        }

        private void writeEdges(ICjGraph g, boolean directed) {
            String op = directed ? "->" : "--";
            g.edges().forEach(e -> {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() < 2) return; // need at least 2 endpoints
                eps = new ArrayList<>(eps);
                if (directed) {
                    // Preserve source->target order: IN (source) endpoints first, OUT (target) last.
                    // This keeps the DOT '->' direction faithful to the CJ edge.
                    eps.sort(Comparator.comparingInt(CjDocument2Dot::directedRank));
                } else {
                    // sort endpoints to get stable order by node id
                    eps.sort(Comparator.comparing(ICjEndpoint::node, Comparator.nullsLast(String::compareTo)));
                }
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
            if (port != null && !port.isEmpty()) return node + ":" + portRef(ep.node(), port);
            // a port Graphviz ignores (see DotLines2CjDocument): write it back as it was
            String ignored = dataString(ep, DotPorts.DATA_PORT);
            if (ignored == null) return node;
            String compass = dataString(ep, DotPorts.DATA_COMPASS);
            return node + ":" + idOrString(ignored) + (compass == null ? "" : ":" + idOrString(compass));
        }

        /** DOT syntax for CJ port {@code portId}: a compass port nested in port {@code p} is written {@code p:compass}. */
        private String portRef(@Nullable String nodeId, String portId) {
            ICjNode node = nodeId == null ? null : nodesById.get(nodeId);
            if (node != null) {
                for (ICjPort parent : node.ports().toList()) {
                    for (ICjPort child : parent.ports().toList()) {
                        if (!portId.equals(child.id())) continue;
                        String compass = dataString(child, DotPorts.DATA_COMPASS);
                        if (compass != null && parent.id() != null) return idOrString(parent.id()) + ":" + idOrString(compass);
                    }
                }
            }
            return idOrString(portId);
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

        private boolean isHtmlLabel(ICjLabelEntry entry) {
            String v = entry.value();
            return dataBoolean(entry, DotPorts.DATA_HTML) && v.startsWith("<") && v.endsWith(">");
        }

        /** The ports a node declares, i.e. all its ports except compass points, flattened. */
        private List<String> declaredPorts(ICjNode n) {
            List<String> ids = new ArrayList<>();
            n.ports().forEach(p -> collectDeclaredPorts(p, ids));
            return ids;
        }

        private void collectDeclaredPorts(ICjPort p, List<String> ids) {
            if (p.id() != null && dataString(p, DotPorts.DATA_COMPASS) == null) ids.add(p.id());
            p.ports().forEach(child -> collectDeclaredPorts(child, ids));
        }

        /** A quoted record label: the given label text, then one field per port. */
        private String recordLabel(@Nullable String label, List<String> ports) {
            List<String> fields = new ArrayList<>();
            if (label != null && !label.isEmpty()) fields.add(escapeRecord(label));
            for (String p : ports) fields.add("<" + escapeRecord(p) + "> " + escapeRecord(p));
            // not quote(): the record escapes must reach Graphviz as single backslashes
            return '"' + String.join("|", fields).replace("\"", "\\\"") + '"';
        }

        private String escapeRecord(String s) {
            return s.replaceAll("([{}|<>\\\\])", "\\\\$1");
        }

        private @Nullable ICjLabelEntry firstLabelEntry(ICjHasLabel hasLabel) {
            List<ICjLabelEntry> entries = hasLabel.labelEntries();
            return entries.isEmpty() ? null : entries.get(0);
        }

        private @Nullable String firstLabel(ICjHasLabel hasLabel) {
            List<ICjLabelEntry> entries = hasLabel.labelEntries();
            if (entries.isEmpty()) return null;
            String v = entries.get(0).value();
            return v;
        }
    }
}
