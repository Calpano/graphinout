package com.graphinout.reader.mermaid.parsers;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid {@code flowchart} / {@code graph} diagrams.
 * <p>
 * Supported syntax (v1):
 * <ul>
 *   <li>Header: {@code flowchart LR|TD|RL|BT|TB} or {@code graph LR|...}</li>
 *   <li>Node shapes: {@code id[Label]}, {@code id(Label)}, {@code id((Label))}, {@code id{Label}},
 *       {@code id{{Label}}}, {@code id[[Label]]}, {@code id[(Label)]}, {@code id>Label]},
 *       {@code id([Label])}</li>
 *   <li>Edges: {@code A --> B}, {@code A -- text --> B}, {@code A -->|text| B}, {@code A --- B},
 *       {@code A -. text .-> B}, {@code A === B}, {@code A ==text==> B},
 *       {@code A --o B}, {@code A --x B}</li>
 *   <li>Edge chains: {@code A --> B --> C}</li>
 *   <li>{@code subgraph name [Title]} ... {@code end} (parsed but flattened; nodes emitted in document order)</li>
 *   <li>Inline class assignments: {@code class A,B styleClass} (ignored)</li>
 *   <li>{@code direction LR}</li>
 *   <li>{@code style id fill:#fff} (ignored)</li>
 *   <li>{@code linkStyle 0 stroke:red} (ignored)</li>
 * </ul>
 */
public class FlowchartParser {

    /** Edge token, e.g. "-->", "---", "==>", "===", "-.->", "-.-", "--o", "--x". */
    private static final Pattern EDGE_TOKEN = Pattern.compile(
            "(?<arrow>" +
                    "<?-{2,}>?" +           // --, ---, -->, <-->, ---->, <----
                    "|<?={2,}>?" +          // ==, ===, ==>, <==>
                    "|<?-\\.+-?>?" +        // -.->, -.-, -.->
                    "|--[ox]" +             // --o, --x
                    ")"
    );

    /** Matches optional inline "|label|" or "|text|" after the arrow. */
    private static final Pattern PIPE_LABEL = Pattern.compile("\\|([^|]*)\\|");

    /** Matches an inline label like "-- text -->" embedded in the arrow. We post-process this by splitting. */
    private static final Pattern ARROW_WITH_MIDDLE_LABEL = Pattern.compile(
            "(?<lead>-{2,}|={2,}|-\\.+-?)" +
            "\\s*(?<text>[^-=<>|]+?)\\s*" +
            "(?<trail>-{2,}>?|={2,}>?|\\.+-?>)"
    );

    /** Identifier (used in shapes). Lenient: anything but whitespace and shape delimiters. */
    private static final Pattern NODE_TOKEN = Pattern.compile(
            "(?<id>[A-Za-z0-9_\\-]+)" +
            "(?:" +
                "\\[\\[(?<sqsq>[^\\]]*)\\]\\]" +     // [[label]] subroutine
                "|\\[\\((?<sqr>[^)]*)\\)\\]" +       // [(label)] cylinder
                "|\\(\\((?<rr>[^)]*)\\)\\)" +        // ((label)) circle
                "|\\(\\[(?<rsq>[^\\]]*)\\]\\)" +     // ([label]) stadium
                "|\\{\\{(?<brbr>[^}]*)\\}\\}" +      // {{label}} hexagon
                "|\\[(?<sq>[^\\]]*)\\]" +            // [label] rect
                "|\\((?<r>[^)]*)\\)" +               // (label) rounded
                "|\\{(?<br>[^}]*)\\}" +              // {label} rhombus
                "|>(?<asym>[^\\]]*)\\]" +            // >label] asymmetric
            ")?"
    );

    public void parse(String headerLine, List<String> bodyLines, MermaidParseCtx ctx, int headerLineNumber) {
        // Header gives direction
        String dir = parseDirection(headerLine);
        if (dir != null) {
            // (we could store this as document data; for now leave it as a noop)
        }

        // Subgraph nesting handled flatly — we still record nodes by id.
        int subgraphDepth = 0;
        int ln = headerLineNumber;
        for (String raw : bodyLines) {
            ln++;
            ctx.setLineNumber(ln);
            String line = raw.trim();
            if (line.isEmpty()) continue;
            String low = line.toLowerCase();
            if (low.startsWith("subgraph")) {
                subgraphDepth++;
                continue;
            }
            if (low.equals("end")) {
                if (subgraphDepth > 0) subgraphDepth--;
                continue;
            }
            if (low.startsWith("direction ")) continue;
            if (low.startsWith("style ") || low.startsWith("linkstyle ")
                    || low.startsWith("classdef ") || low.startsWith("class ")
                    || low.startsWith("click ")) continue;

            parseStatement(line, ctx);
        }
    }

    /** Parse one logical statement, which may contain a chain of nodes and edges. */
    void parseStatement(String line, MermaidParseCtx ctx) {
        // strip trailing ';'
        if (line.endsWith(";")) line = line.substring(0, line.length() - 1).trim();

        // Tokenize: alternating node | edge | node | edge ...
        List<Object> tokens = tokenize(line, ctx);
        if (tokens.isEmpty()) return;

        // Must start with a node
        if (!(tokens.get(0) instanceof NodeRef)) {
            ctx.warn("Statement does not start with a node: " + line);
            return;
        }
        // walk pairs: node, (edge, node)*
        NodeRef prev = (NodeRef) tokens.get(0);
        ctx.ensureNode(prev.id, prev.label);
        for (int i = 1; i + 1 < tokens.size(); i += 2) {
            Object eTok = tokens.get(i);
            Object nTok = tokens.get(i + 1);
            if (!(eTok instanceof EdgeRef) || !(nTok instanceof NodeRef)) {
                ctx.warn("Malformed chain near token " + i + " in: " + line);
                break;
            }
            EdgeRef edge = (EdgeRef) eTok;
            NodeRef next = (NodeRef) nTok;
            ctx.ensureNode(next.id, next.label);
            ctx.addEdge(prev.id, next.id, edge.label);
            prev = next;
        }
    }

    /** Split a single statement into a sequence of NodeRef / EdgeRef tokens. */
    List<Object> tokenize(String line, MermaidParseCtx ctx) {
        List<Object> out = new ArrayList<>();
        int pos = 0;
        int n = line.length();
        while (pos < n) {
            while (pos < n && Character.isWhitespace(line.charAt(pos))) pos++;
            if (pos >= n) break;

            // Try arrow with embedded middle label: e.g. "-- text -->" or "-. text .->" or "==text==>"
            Matcher midM = ARROW_WITH_MIDDLE_LABEL.matcher(line).region(pos, n);
            if (midM.lookingAt()) {
                String txt = midM.group("text");
                out.add(new EdgeRef(txt));
                pos = midM.end();
                // Optional immediate "|label|"
                Matcher pl = PIPE_LABEL.matcher(line).region(pos, n);
                if (pl.lookingAt()) {
                    EdgeRef last = (EdgeRef) out.get(out.size() - 1);
                    if (last.label == null) last.label = pl.group(1);
                    pos = pl.end();
                }
                continue;
            }

            // Try plain arrow token: --, -->, ===, ==>, ---x, --o, -.-, -.->
            Matcher arrM = EDGE_TOKEN.matcher(line).region(pos, n);
            if (arrM.lookingAt()) {
                EdgeRef edge = new EdgeRef(null);
                out.add(edge);
                pos = arrM.end();
                // Optional immediate "|label|"
                while (pos < n && Character.isWhitespace(line.charAt(pos))) pos++;
                Matcher pl = PIPE_LABEL.matcher(line).region(pos, n);
                if (pl.lookingAt()) {
                    edge.label = pl.group(1);
                    pos = pl.end();
                }
                continue;
            }

            // Otherwise parse a node
            Matcher nm = NODE_TOKEN.matcher(line).region(pos, n);
            if (nm.lookingAt()) {
                String id = nm.group("id");
                String label = firstNonNull(
                        nm.group("sqsq"), nm.group("sqr"), nm.group("rr"), nm.group("rsq"),
                        nm.group("brbr"), nm.group("sq"), nm.group("r"), nm.group("br"),
                        nm.group("asym"));
                if (label != null) label = stripQuotes(label.trim());
                out.add(new NodeRef(id, label));
                pos = nm.end();
                // Optional: '&' chains (Mermaid's "A & B --> C"). Treat each via separate tokens.
                while (pos < n && Character.isWhitespace(line.charAt(pos))) pos++;
                if (pos < n && line.charAt(pos) == '&') {
                    pos++;
                    continue;
                }
                continue;
            }

            // Nothing matched; skip this char to make progress.
            pos++;
        }
        return out;
    }

    private static @Nullable String firstNonNull(String... vals) {
        for (String v : vals) if (v != null) return v;
        return null;
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static @Nullable String parseDirection(String headerLine) {
        String s = headerLine.trim();
        // e.g. "flowchart LR" or "graph TD"
        int sp = s.indexOf(' ');
        if (sp < 0) return null;
        String d = s.substring(sp + 1).trim().toUpperCase();
        if (d.isEmpty()) return null;
        // Common directions
        return switch (d) {
            case "LR", "RL", "TB", "TD", "BT" -> d;
            default -> null;
        };
    }

    private static final class NodeRef {
        final String id;
        @Nullable final String label;
        NodeRef(String id, @Nullable String label) {this.id = id; this.label = label;}
    }

    private static final class EdgeRef {
        @Nullable String label;
        EdgeRef(@Nullable String label) {this.label = label;}
    }
}
