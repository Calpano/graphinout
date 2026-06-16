package com.graphinout.reader.mermaid;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** In-memory model used by the (flowchart-only) writer. */
public class MermaidDoc {

    public static class MermaidNode {
        public final String id;
        public final @Nullable String label;
        /** Subgraph children (graphs nested inside this node). Empty for plain nodes. */
        public final List<MermaidSubgraph> subgraphs = new ArrayList<>();

        public MermaidNode(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }
    }

    /** A {@code subgraph ... end} block, carrying its own nodes/edges/subgraphs. */
    public static class MermaidSubgraph {
        public final String id;
        public final @Nullable String label;
        public final List<MermaidNode> nodes = new ArrayList<>();
        public final List<MermaidEdge> edges = new ArrayList<>();

        public MermaidSubgraph(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class MermaidEdge {
        public final String sourceId;
        public final String targetId;
        public final @Nullable String label;
        /** false = undirected ({@code ---}); true = directed ({@code -->}). */
        public final boolean directed;

        public MermaidEdge(String sourceId, String targetId, @Nullable String label, boolean directed) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
            this.directed = directed;
        }
    }

    public String direction = "LR";
    public final List<MermaidNode> nodes = new ArrayList<>();
    public final List<MermaidEdge> edges = new ArrayList<>();

    public String toMermaid() {
        StringBuilder b = new StringBuilder();
        b.append("flowchart ").append(direction).append('\n');
        for (MermaidNode n : nodes) {
            appendNode(b, n, 1);
        }
        for (MermaidEdge e : edges) {
            appendEdge(b, e, 1);
        }
        return b.toString();
    }

    private static void indent(StringBuilder b, int depth) {
        for (int i = 0; i < depth; i++) b.append("    ");
    }

    private void appendNode(StringBuilder b, MermaidNode n, int depth) {
        if (n.subgraphs.isEmpty()) {
            indent(b, depth);
            b.append(sanitizeId(n.id));
            if (n.label != null && !n.label.isEmpty() && !n.label.equals(n.id)) {
                b.append('[').append(escapeLabel(n.label)).append(']');
            }
            b.append('\n');
        } else {
            // A node carrying nested graphs is rendered as one subgraph block per nested graph.
            // We render the node's own id as the subgraph id (so it round-trips as a node-with-graph).
            for (MermaidSubgraph sg : n.subgraphs) {
                appendSubgraph(b, n.id, sg, depth);
            }
        }
    }

    private void appendSubgraph(StringBuilder b, String nodeId, MermaidSubgraph sg, int depth) {
        indent(b, depth);
        b.append("subgraph ").append(sanitizeId(nodeId));
        if (sg.label != null && !sg.label.isEmpty()) {
            b.append(" [").append(escapeLabel(sg.label)).append(']');
        }
        b.append('\n');
        for (MermaidNode child : sg.nodes) {
            appendNode(b, child, depth + 1);
        }
        for (MermaidEdge e : sg.edges) {
            appendEdge(b, e, depth + 1);
        }
        indent(b, depth);
        b.append("end\n");
    }

    private void appendEdge(StringBuilder b, MermaidEdge e, int depth) {
        indent(b, depth);
        b.append(sanitizeId(e.sourceId));
        String arrow = e.directed ? "-->" : "---";
        if (e.label != null && !e.label.isEmpty()) {
            b.append(' ').append(arrow).append('|').append(escapeLabel(e.label)).append("| ");
        } else {
            b.append(' ').append(arrow).append(' ');
        }
        b.append(sanitizeId(e.targetId)).append('\n');
    }

    /** Make an id safe for use as a Mermaid node id (letters, digits, underscore, hyphen only). */
    static String sanitizeId(String id) {
        StringBuilder b = new StringBuilder(id.length());
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                b.append(c);
            } else {
                b.append('_');
            }
        }
        return b.length() == 0 ? "_" : b.toString();
    }

    /** Strip characters that would break a Mermaid label (pipes, brackets). */
    static String escapeLabel(String s) {
        return s.replace('|', '/')
                .replace('[', '(')
                .replace(']', ')')
                .replace('\n', ' ');
    }
}
