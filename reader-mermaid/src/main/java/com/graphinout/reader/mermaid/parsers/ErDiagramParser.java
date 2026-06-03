package com.graphinout.reader.mermaid.parsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code erDiagram}. v1 captures entities + relationships; attribute blocks
 * {@code ENT { type name PK ... }} are parsed but the attributes themselves are not promoted to CJ data.
 */
public class ErDiagramParser {

    private static final Pattern REL = Pattern.compile(
            "^(?<left>[A-Za-z0-9_]+)\\s+(?<lcard>[|}{o\\-]{2,4})--(?<rcard>[|}{o\\-]{2,4})\\s+(?<right>[A-Za-z0-9_]+)(?:\\s*:\\s*(?<label>.+))?$"
    );

    /** A simpler matcher: anything with "--" between two identifiers. */
    private static final Pattern REL_LOOSE = Pattern.compile(
            "^(?<left>[A-Za-z0-9_]+)\\s+\\S+--\\S+\\s+(?<right>[A-Za-z0-9_]+)(?:\\s*:\\s*(?<label>.+))?$"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        boolean inEntityBlock = false;
        String currentEntity = null;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (inEntityBlock) {
                if (line.equals("}")) { inEntityBlock = false; currentEntity = null; }
                continue;
            }

            // ENTITY { ... } block opener
            if (line.endsWith("{")) {
                String name = line.substring(0, line.length() - 1).trim();
                if (name.matches("[A-Za-z0-9_]+")) {
                    ctx.ensureNode(name);
                    currentEntity = name;
                    inEntityBlock = true;
                    continue;
                }
            }

            Matcher rm = REL.matcher(line);
            if (rm.matches()) {
                String left = rm.group("left");
                String right = rm.group("right");
                String label = rm.group("label");
                ctx.addEdge(left, right, label);
                continue;
            }
            Matcher rl = REL_LOOSE.matcher(line);
            if (rl.matches()) {
                ctx.addEdge(rl.group("left"), rl.group("right"), rl.group("label"));
                continue;
            }
            // Lone entity declaration on its own line
            if (line.matches("[A-Za-z0-9_]+")) {
                ctx.ensureNode(line);
            }
        }
    }
}
