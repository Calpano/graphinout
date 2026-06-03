package com.graphinout.reader.mermaid.parsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code classDiagram}. Captures classes and relationships only; class members are stored
 * implicitly via node creation (members in {@code class Foo { ... }} blocks are ignored in v1).
 */
public class ClassDiagramParser {

    /** A relation token: {@code <|--}, {@code *--}, {@code o--}, {@code -->}, {@code --|>}, {@code --*}, {@code --o}, {@code ..>}, {@code <..}, {@code --}, {@code ..}, with optional cardinality strings around them. */
    private static final Pattern REL_LINE = Pattern.compile(
            "^\\s*(?<left>\\S+?)" +
            "(?:\\s+\"(?<lcard>[^\"]*)\")?" +
            "\\s+(?<rel><\\|--|--\\|>|\\*--|--\\*|o--|--o|<\\.\\.|\\.\\.>|<-->|<--|-->|--|\\.\\.)\\s+" +
            "(?:\"(?<rcard>[^\"]*)\"\\s+)?" +
            "(?<right>\\S+?)" +
            "(?:\\s*:\\s*(?<label>.+?))?\\s*$"
    );

    private static final Pattern CLASS_DECL = Pattern.compile(
            "^\\s*class\\s+(?<id>[A-Za-z0-9_]+)(?:\\s*\\{[^}]*\\})?\\s*$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        boolean inBlock = false;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;
            // skip lines inside a "class Foo { ... }" block that opens on its own line
            if (line.endsWith("{") && !line.contains("}")) {
                Matcher m = Pattern.compile("^class\\s+(?<id>[A-Za-z0-9_]+)\\s*\\{\\s*$").matcher(line);
                if (m.matches()) {
                    ctx.ensureNode(m.group("id"));
                    inBlock = true;
                    continue;
                }
            }
            if (inBlock) {
                if (line.equals("}")) inBlock = false;
                continue;
            }
            if (line.startsWith("note ") || line.startsWith("namespace ")) continue;
            if (line.startsWith("direction ")) continue;

            // Member declarations like "Foo : +method()" → declare Foo
            int colon = line.indexOf(':');
            if (colon > 0 && !line.contains("--") && !line.contains("..") && !line.startsWith("class ")) {
                String left = line.substring(0, colon).trim();
                if (left.matches("[A-Za-z0-9_]+")) {
                    ctx.ensureNode(left);
                    continue;
                }
            }

            Matcher cm = CLASS_DECL.matcher(line);
            if (cm.matches()) {
                ctx.ensureNode(cm.group("id"));
                continue;
            }

            Matcher rm = REL_LINE.matcher(line);
            if (rm.matches()) {
                String left = stripBrackets(rm.group("left"));
                String right = stripBrackets(rm.group("right"));
                String label = rm.group("label");
                ctx.ensureNode(left);
                ctx.ensureNode(right);
                ctx.addEdge(left, right, label);
                continue;
            }
            // Otherwise silently ignore (notes etc.)
        }
    }

    private static String stripBrackets(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
