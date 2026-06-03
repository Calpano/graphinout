package com.graphinout.reader.mermaid.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code C4Context} / {@code C4Container} / {@code C4Component} / {@code C4Dynamic} diagrams.
 * <p>
 * Recognized constructors (variants like {@code _Ext}, {@code _Db}, {@code _Queue} all map to a typed node):
 * <ul>
 *   <li>{@code Person(alias, "label", "descr")}</li>
 *   <li>{@code System(alias, "label", "descr")}</li>
 *   <li>{@code Container(alias, "label", "tech", "descr")}</li>
 *   <li>{@code Component(alias, "label", "tech", "descr")}</li>
 *   <li>{@code Rel(from, to, "label", "tech")}</li>
 *   <li>{@code Boundary(alias, "label", "type")} (with optional trailing {@code {})</li>
 * </ul>
 */
public class C4Parser {

    private static final Pattern CALL = Pattern.compile(
            "^(?<name>[A-Za-z][A-Za-z0-9_]*)\\((?<args>.*)\\)\\s*\\{?\\s*$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty() || line.equals("}")) continue;

            Matcher m = CALL.matcher(line);
            if (!m.matches()) continue;
            String name = m.group("name");
            List<String> args = splitArgs(m.group("args"));

            String low = name.toLowerCase();
            if (low.startsWith("rel")) {
                if (args.size() >= 2) {
                    String from = args.get(0);
                    String to = args.get(1);
                    String label = args.size() >= 3 ? args.get(2) : null;
                    ctx.addEdge(from, to, label);
                }
                continue;
            }
            if (low.endsWith("boundary") || low.equals("boundary")) {
                // emit the boundary as a node too
                if (!args.isEmpty()) ctx.ensureNode(args.get(0), args.size() >= 2 ? args.get(1) : null);
                continue;
            }
            // Person / System / Container / Component / Node / Database / Queue ...
            if (!args.isEmpty()) {
                String alias = args.get(0);
                String label = args.size() >= 2 ? args.get(1) : null;
                ctx.ensureNode(alias, label);
            }
        }
    }

    /** Split a C4 call argument list. Respects quoted strings. */
    static List<String> splitArgs(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                out.add(cur.toString().trim());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (cur.length() > 0 || !out.isEmpty()) out.add(cur.toString().trim());
        return out;
    }
}
