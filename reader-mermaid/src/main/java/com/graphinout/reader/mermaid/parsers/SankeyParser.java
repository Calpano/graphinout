package com.graphinout.reader.mermaid.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Mermaid {@code sankey-beta}. Each non-blank, non-comment line is a CSV row
 * {@code source,target,value} (commas inside double-quoted strings are preserved).
 */
public class SankeyParser {

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        int ln = headerLineNumber;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;
            List<String> cols = csvSplit(line);
            if (cols.size() < 2) continue;
            String src = cols.get(0).trim();
            String tgt = cols.get(1).trim();
            String val = cols.size() >= 3 ? cols.get(2).trim() : null;
            if (src.isEmpty() || tgt.isEmpty()) continue;
            ctx.addEdge(src, tgt, val);
        }
    }

    static List<String> csvSplit(String s) {
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
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }
}
