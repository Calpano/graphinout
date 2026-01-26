package com.graphinout.reader.tgf;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TgfDoc {

    public static class TgfNode {

        String id;
        @Nullable String label;

        public TgfNode(String id, @Nullable String label) {
            this.id = id;
            this.label = label;
        }

    }

    public static class TgfEdge {

        String sourceId;
        String targetId;
        @Nullable String label;

        public TgfEdge(String sourceId, String targetId, @Nullable String label) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
        }

    }

    public static final char HASHMARK = '#';
    public static final char SPACE = ' ';

    final List<TgfNode> nodes = new ArrayList<>();
    final List<TgfEdge> edges = new ArrayList<>();

    public String toTgf() {
        StringBuilder b = new StringBuilder();
        nodes.forEach(n -> {
            b.append(n.id);
            if (n.label != null && !n.label.isEmpty()) {
                b.append(SPACE).append(n.label);
            }
            b.append('\n');
        });
        b.append(HASHMARK).append('\n');
        edges.forEach(e -> {
            b.append(e.sourceId);
            b.append(SPACE);
            b.append(e.targetId);
            if (e.label != null && !e.label.isEmpty()) {
                b.append(SPACE).append(e.label);
            }
            b.append('\n');
        });
        return b.toString();
    }

}
