package com.graphinout.reader.mermaid.parsers;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code mindmap}.
 * <p>
 * The hierarchy is determined by indentation. Each indent level becomes a child of the most recent
 * less-indented node. Shapes around the label (e.g. {@code Root((cloud))}) are stripped to extract the label.
 */
public class MindmapParser {

    /**
     * label-only pattern: capture the inner label whether it's wrapped in any of the known shape brackets,
     * or bare text.
     */
    private static final Pattern SHAPE = Pattern.compile(
            "^(?:\\[\\[(?<sqsq>[^\\]]*)\\]\\]" +
            "|\\(\\((?<rr>[^)]*)\\)\\)" +
            "|\\)\\)(?<bang>[^(]*)\\(\\(" +
            "|\\)(?<rl>[^(]*)\\(" +
            "|\\{\\{(?<brbr>[^}]*)\\}\\}" +
            "|\\[(?<sq>[^\\]]*)\\]" +
            "|\\((?<r>[^)]*)\\)" +
            "|\\{(?<br>[^}]*)\\}" +
            "|(?<txt>.+))$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        // Stack of (indent, nodeId)
        List<Entry> stack = new ArrayList<>();
        int ln = headerLineNumber;
        int counter = 0;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            if (raw.isBlank()) continue;
            int indent = leadingWhitespace(raw);
            String trimmed = raw.trim();
            String label = extractLabel(trimmed);
            if (label == null || label.isEmpty()) continue;
            String id = "mm_" + (++counter);
            ctx.ensureNode(id, label);

            // Pop until the top is less indented than current
            while (!stack.isEmpty() && stack.get(stack.size() - 1).indent >= indent) {
                stack.remove(stack.size() - 1);
            }
            if (!stack.isEmpty()) {
                ctx.addEdge(stack.get(stack.size() - 1).id, id, null);
            }
            stack.add(new Entry(indent, id));
        }
    }

    static int leadingWhitespace(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) i++;
        return i;
    }

    static @Nullable String extractLabel(String s) {
        Matcher m = SHAPE.matcher(s);
        if (!m.matches()) return s;
        for (String g : new String[]{"sqsq", "rr", "bang", "rl", "brbr", "sq", "r", "br", "txt"}) {
            String v = m.group(g);
            if (v != null) return v.trim();
        }
        return s;
    }

    private static final class Entry {
        final int indent;
        final String id;
        Entry(int indent, String id) {this.indent = indent; this.id = id;}
    }
}
