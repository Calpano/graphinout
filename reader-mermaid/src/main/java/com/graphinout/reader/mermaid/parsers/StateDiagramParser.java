package com.graphinout.reader.mermaid.parsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code stateDiagram} / {@code stateDiagram-v2}.
 * <p>
 * {@code [*]} is the start/end pseudo-state and is given the id {@code __start_end__}.
 * Composite states are parsed but flattened (children are emitted as siblings).
 */
public class StateDiagramParser {

    static final String STAR = "__start_end__";

    private static final Pattern TRANSITION = Pattern.compile(
            "^(?<left>\\[\\*\\]|[A-Za-z0-9_]+)\\s*-->\\s*(?<right>\\[\\*\\]|[A-Za-z0-9_]+)(?:\\s*:\\s*(?<label>.+))?$"
    );

    private static final Pattern STATE_DECL = Pattern.compile(
            "^state\\s+(?:\"(?<long>[^\"]+)\"\\s+as\\s+(?<alias>[A-Za-z0-9_]+)|(?<id>[A-Za-z0-9_]+)(?:\\s*\\{)?)\\s*$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.equals("}")) continue;
            if (line.startsWith("note ") || line.startsWith("direction ")) continue;

            Matcher tm = TRANSITION.matcher(line);
            if (tm.matches()) {
                String left = normalize(tm.group("left"));
                String right = normalize(tm.group("right"));
                String label = tm.group("label");
                ctx.addEdge(left, right, label);
                continue;
            }

            Matcher sd = STATE_DECL.matcher(line);
            if (sd.matches()) {
                String alias = sd.group("alias");
                String id = sd.group("id");
                String longLabel = sd.group("long");
                if (alias != null) ctx.ensureNode(alias, longLabel);
                else if (id != null) ctx.ensureNode(id);
            }
        }
    }

    private static String normalize(String s) {
        return s.equals("[*]") ? STAR : s;
    }
}
