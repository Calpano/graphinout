package com.graphinout.reader.mermaid.parsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code architecture-beta}.
 * <p>
 * Recognized declarations:
 * <ul>
 *   <li>{@code group <id>(<icon>)[<label>]} optionally {@code in <parentId>}</li>
 *   <li>{@code service <id>(<icon>)[<label>]} optionally {@code in <groupId>}</li>
 *   <li>{@code junction <id>} optionally {@code in <groupId>}</li>
 *   <li>Connections: {@code <id>:<side> -- <side>:<id>} or with directed arrow {@code -->}</li>
 * </ul>
 */
public class ArchitectureParser {

    private static final Pattern DECL = Pattern.compile(
            "^(?<kind>group|service|junction)\\s+(?<id>[A-Za-z0-9_]+)" +
            "(?:\\((?<icon>[^)]*)\\))?" +
            "(?:\\[(?<label>[^\\]]*)\\])?" +
            "(?:\\s+in\\s+(?<parent>[A-Za-z0-9_]+))?\\s*$"
    );

    private static final Pattern CONN = Pattern.compile(
            "^(?<left>[A-Za-z0-9_]+)(?::(?<lside>[A-Za-z]+))?" +
            "\\s*(?<arrow>-->|<-->|--)\\s*" +
            "(?<right>[A-Za-z0-9_]+)(?::(?<rside>[A-Za-z]+))?" +
            "(?:\\s*\\[(?<label>[^\\]]*)\\])?\\s*$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;

            Matcher dm = DECL.matcher(line);
            if (dm.matches()) {
                ctx.ensureNode(dm.group("id"), dm.group("label"));
                String parent = dm.group("parent");
                if (parent != null) ctx.addEdge(parent, dm.group("id"), "contains");
                continue;
            }
            // Conn syntax also matches "a:R -- L:b". Try reordering side hints into the conn matcher above.
            Matcher cm = CONN.matcher(line.replace(" -- ", "--").replace("-- ", "--").replace(" --", "--"));
            if (cm.matches()) {
                ctx.addEdge(cm.group("left"), cm.group("right"), cm.group("label"));
            }
        }
    }
}
