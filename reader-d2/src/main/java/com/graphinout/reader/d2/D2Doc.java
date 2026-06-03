package com.graphinout.reader.d2;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** In-memory model for the D2 writer. */
public class D2Doc {

    public static class D2Node {
        public final String id;
        public final @Nullable String label;

        public D2Node(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class D2Edge {
        public final String sourceId;
        public final String targetId;
        public final @Nullable String label;

        public D2Edge(String sourceId, String targetId, @Nullable String label) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
        }
    }

    public final List<D2Node> nodes = new ArrayList<>();
    public final List<D2Edge> edges = new ArrayList<>();

    public String toD2() {
        StringBuilder b = new StringBuilder();
        for (D2Node n : nodes) {
            String id = quoteIfNeeded(n.id);
            if (n.label != null && !n.label.isEmpty() && !n.label.equals(n.id)) {
                b.append(id).append(": ").append(quoteIfNeeded(n.label)).append('\n');
            } else {
                b.append(id).append('\n');
            }
        }
        for (D2Edge e : edges) {
            b.append(quoteIfNeeded(e.sourceId))
             .append(" -> ")
             .append(quoteIfNeeded(e.targetId));
            if (e.label != null && !e.label.isEmpty()) {
                b.append(": ").append(quoteIfNeeded(e.label));
            }
            b.append('\n');
        }
        return b.toString();
    }

    /** Quote an id/label when it contains characters that would confuse the D2 lexer. */
    static String quoteIfNeeded(String s) {
        if (s == null) return "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') continue;
            // Anything else triggers quoting (so dotted paths, spaces, ':', '/' etc. are preserved literally).
            return "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
    }
}
