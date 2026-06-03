package com.graphinout.reader.mermaid;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** In-memory model used by the (flowchart-only) writer. */
public class MermaidDoc {

    public static class MermaidNode {
        public final String id;
        public final @Nullable String label;

        public MermaidNode(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class MermaidEdge {
        public final String sourceId;
        public final String targetId;
        public final @Nullable String label;

        public MermaidEdge(String sourceId, String targetId, @Nullable String label) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
        }
    }

    public String direction = "LR";
    public final List<MermaidNode> nodes = new ArrayList<>();
    public final List<MermaidEdge> edges = new ArrayList<>();

    public String toMermaid() {
        StringBuilder b = new StringBuilder();
        b.append("flowchart ").append(direction).append('\n');
        for (MermaidNode n : nodes) {
            b.append("    ").append(sanitizeId(n.id));
            if (n.label != null && !n.label.isEmpty() && !n.label.equals(n.id)) {
                b.append('[').append(escapeLabel(n.label)).append(']');
            }
            b.append('\n');
        }
        for (MermaidEdge e : edges) {
            b.append("    ").append(sanitizeId(e.sourceId));
            if (e.label != null && !e.label.isEmpty()) {
                b.append(" -->|").append(escapeLabel(e.label)).append("| ");
            } else {
                b.append(" --> ");
            }
            b.append(sanitizeId(e.targetId)).append('\n');
        }
        return b.toString();
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
