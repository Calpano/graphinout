package com.graphinout.reader.d2;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory model for the D2 writer. Supports nested containers and attribute maps. */
public class D2Doc {

    public static class D2Node {
        public final String id;
        public final @Nullable String label;
        /** Attribute key/value pairs, emitted as {@code style.<key>: <value>}. */
        public final Map<String, String> attributes = new LinkedHashMap<>();
        /** Child shapes (nested container contents). */
        public final List<D2Node> children = new ArrayList<>();
        public final List<D2Edge> childEdges = new ArrayList<>();

        public D2Node(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }

        boolean hasBody() {
            return !attributes.isEmpty() || !children.isEmpty() || !childEdges.isEmpty();
        }
    }

    public static class D2Edge {
        public final String sourceId;
        public final String targetId;
        public final @Nullable String label;
        /** {@code true} -> emit {@code --} (undirected); {@code false} -> emit {@code ->} (directed). */
        public final boolean undirected;
        public final Map<String, String> attributes = new LinkedHashMap<>();

        public D2Edge(String sourceId, String targetId, @Nullable String label, boolean undirected) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
            this.undirected = undirected;
        }
    }

    /** Top-level shapes and edges. */
    public final List<D2Node> nodes = new ArrayList<>();
    public final List<D2Edge> edges = new ArrayList<>();

    public String toD2() {
        StringBuilder b = new StringBuilder();
        for (D2Node n : nodes) writeNode(b, n, 0);
        for (D2Edge e : edges) writeEdge(b, e, 0);
        return b.toString();
    }

    private void writeNode(StringBuilder b, D2Node n, int indent) {
        indent(b, indent);
        String id = quoteIfNeeded(n.id);
        boolean hasLabel = n.label != null && !n.label.isEmpty() && !n.label.equals(n.id);
        if (!n.hasBody()) {
            if (hasLabel) {
                b.append(id).append(": ").append(quoteIfNeeded(n.label)).append('\n');
            } else {
                b.append(id).append('\n');
            }
            return;
        }
        // container / map body
        b.append(id).append(": ");
        if (hasLabel) b.append(quoteIfNeeded(n.label)).append(' ');
        b.append("{\n");
        for (Map.Entry<String, String> a : n.attributes.entrySet()) {
            indent(b, indent + 1);
            b.append("style.").append(quoteIfNeeded(a.getKey()))
             .append(": ").append(quoteIfNeeded(a.getValue())).append('\n');
        }
        for (D2Node child : n.children) writeNode(b, child, indent + 1);
        for (D2Edge e : n.childEdges) writeEdge(b, e, indent + 1);
        indent(b, indent);
        b.append("}\n");
    }

    private void writeEdge(StringBuilder b, D2Edge e, int indent) {
        indent(b, indent);
        b.append(quoteIfNeeded(e.sourceId))
         .append(e.undirected ? " -- " : " -> ")
         .append(quoteIfNeeded(e.targetId));
        if (e.label != null && !e.label.isEmpty()) {
            b.append(": ").append(quoteIfNeeded(e.label));
        }
        if (!e.attributes.isEmpty()) {
            b.append(" {\n");
            for (Map.Entry<String, String> a : e.attributes.entrySet()) {
                indent(b, indent + 1);
                b.append("style.").append(quoteIfNeeded(a.getKey()))
                 .append(": ").append(quoteIfNeeded(a.getValue())).append('\n');
            }
            indent(b, indent);
            b.append("}");
        }
        b.append('\n');
    }

    private static void indent(StringBuilder b, int indent) {
        for (int i = 0; i < indent; i++) b.append("  ");
    }

    /** Quote an id/label when it contains characters that would confuse the D2 lexer. */
    static String quoteIfNeeded(String s) {
        if (s == null) return "";
        if (s.isEmpty()) return "\"\"";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') continue;
            // Anything else triggers quoting (so dotted paths, spaces, ':', '/' etc. are preserved literally).
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return s;
    }
}
