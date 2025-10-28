package com.graphinout.reader.dot;

import com.graphinout.base.BaseOutput;
import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.element.ICjGraphMutable;
import com.graphinout.base.cj.element.ICjHasDataMutable;
import com.graphinout.base.cj.element.ICjHasLabelMutable;
import com.graphinout.base.cj.element.ICjNodeMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.reader.Location;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.text.ITextWriter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DOT → CJ parser. Implements a practical subset of the DOT grammar sufficient for the bundled tests:
 * <li>[strict]? (di)graph [id]? '{' stmt_list '}'</li>
 * <li>stmt: node stmt, edge chain, subgraph (named/anonymous), assignment (id '=' id), attr lists [k=v,...]</li>
 * <li>default attribute statements (node[..], edge[..], graph[..]) and rank groups are parsed leniently TODO but
 * ignored</li>
 * <li>TODO comments (//, /* * /, #) are stripped</li>
 * <p>
 * Mapping used:
 * <li>graph/subgraph → ICjGraph (nested)</li>
 * <li>node → ICjNode</li>
 * <li>edge chain → ICjEdge with ordered endpoints (TODO direction left unspecified)</li>
 * <li>attr_list → CjData on the corresponding element; label → ICjLabel (not duplicated in data)</li>
 * <li>port → endpoint.port (TODO compass point, if present, is ignored)</li>
 */
public class DotLines2CjDocument extends BaseOutput implements ITextWriter {

    private record TopLevel(boolean directed, @Nullable String id) {}

    private record NodeRef(String nodeId, @Nullable String port) {}

    private record Attr(String key, String value) {}

    private static final class Parser {

        public static final String DIGRAPH = "digraph";
        public static final String GRAPH = "graph";
        private final String s;
        private int pos = 0;

        Parser(String s) {this.s = s;}

        int position() { return pos; }

        boolean consumeIf(char ch) {
            skipWs();
            if (!eof() && s.charAt(pos) == ch) {
                pos++;
                return true;
            }
            return false;
        }

        void consumeKeyword(String kw) {
            String got = readKeyword();
            if (!kw.equals(got)) throw
                    new IllegalStateException("Expected keyword '" + kw + "' but got '" + got + "'");
        }

        void consumeOptionalSemicolon() {
            skipWs();
            if (!eof() && (s.charAt(pos) == ';' || s.charAt(pos) == ',')) pos++;
        }

        boolean eof() {return pos >= s.length();}

        void expect(char ch) {
            skipWs();
            if (eof() || s.charAt(pos) != ch) throw new IllegalStateException("Expected '" + ch + "' at pos " + pos);
            pos++;
        }

        boolean lookaheadEdgeOp() {
            skipWs();
            return match("->") || match("--");
        }

        boolean lookaheadKeyword(String kw) {
            skipWs();
            int save = pos;
            String id = tryReadId();
            boolean ok = kw.equals(id);
            pos = save;
            return ok;
        }

        boolean match(String t) {
            return s.startsWith(t, pos);
        }

        TopLevel parseTopLevelHeader() {
            skipWs();
            // optional 'strict' ignored
            if (lookaheadKeyword("strict")) readKeyword();
            skipWs();
            String kind = readKeyword();
            boolean directed = switch (kind) {
                case DIGRAPH -> true;
                case GRAPH -> false;
                default -> throw new IllegalStateException("Expected " + GRAPH+" or "+DIGRAPH+", got " + kind);
            };
            String id = tryReadIdOrString();
            return new TopLevel(directed, id);
        }

        char peek() {return s.charAt(pos);}

        String readAngleString() {
            skipWs();
            if (s.charAt(pos) != '<') throw new IllegalStateException("Expected '<' at pos " + pos);
            int depth = 0;
            StringBuilder out = new StringBuilder();
            while (!eof()) {
                char c = s.charAt(pos++);
                out.append(c);
                if (c == '<') depth++;
                else if (c == '>') {
                    depth--;
                    if (depth == 0) break;
                }
            }
            return out.toString();
        }

        List<Attr> readAttrList(@Nullable List<Attr> reuse) {
            skipWs();
            if (peek() != '[') return Collections.emptyList();
            expect('[');
            List<Attr> list = reuse != null ? reuse : new ArrayList<>();
            while (true) {
                skipWs();
                if (!eof() && s.charAt(pos) == ']') {
                    pos++;
                    break;
                }
                String key = readIdOrString();
                skipWs();
                if (consumeIf('=')) {
                    String val = readIdOrString();
                    list.add(new Attr(key, val));
                } else {
                    // attributes without value -> store as key=true
                    list.add(new Attr(key, "true"));
                }
                // separators: comma or semicolon, optionally mixed and with whitespace/newlines
                while (true) {
                    skipWs();
                    if (consumeIf(',')) continue;
                    if (consumeIf(';')) continue;
                    break;
                }
            }
            return list;
        }

        String readEdgeOp() {
            skipWs();
            if (match("->")) {
                pos += 2;
                return "->";
            }
            if (match("--")) {
                pos += 2;
                return "--";
            }
            throw new IllegalStateException("Expected edge operator at pos " + pos);
        }

        String readId() {
            skipWs();
            if (eof()) throw new IllegalStateException("Unexpected EOF reading id");
            int start = pos;
            char c = s.charAt(pos);
            // alphanumeric id starting with letter or underscore
            if (Character.isLetter(c) || c == '_') {
                pos++;
                while (!eof()) {
                    char d = s.charAt(pos);
                    if (Character.isLetterOrDigit(d) || d == '_') pos++;
                    else break;
                }
                return s.substring(start, pos);
            }
            // optional sign for numeric ids
            boolean hadSign = false;
            if ((c == '+' || c == '-')) {
                hadSign = true;
                pos++;
                if (eof()) throw new IllegalStateException("Unexpected EOF after sign reading number");
                c = s.charAt(pos);
            }
            // number (integer or float like 1.5, .5, 1.)
            if (Character.isDigit(c) || c == '.') {
                boolean sawDigit = false;
                if (Character.isDigit(c)) {
                    sawDigit = true;
                    pos++;
                    while (!eof() && Character.isDigit(s.charAt(pos))) pos++;
                }
                // optional decimal point
                if (!eof() && s.charAt(pos) == '.') {
                    pos++;
                    while (!eof() && Character.isDigit(s.charAt(pos))) { pos++; sawDigit = true; }
                }
                if (!sawDigit) {
                    // we had "." but no digits around -> not a valid number
                    throw new IllegalStateException("Invalid number starting at pos " + start);
                }
                return s.substring(start, pos);
            }
            throw new IllegalStateException("Invalid id start at pos " + pos);
        }

        String readIdOrString() {
            skipWs();
            if (!eof()) {
                char c = s.charAt(pos);
                if (c == '"') {
                    return readQuotedString();
                } else if (c == '<') {
                    return readAngleString();
                }
            }
            return readId();
        }

        String readKeyword() {
            String id = readId();
            return id;
        }

        String readQuotedString() {
            skipWs();
            if (s.charAt(pos) != '"') throw new IllegalStateException("Expected '\\\"' at pos " + pos);
            pos++;
            StringBuilder out = new StringBuilder();
            while (!eof()) {
                char c = s.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && !eof()) {
                    char n = s.charAt(pos++);
                    out.append(n);
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        void skipWs() {
            while (!eof()) {
                char c = s.charAt(pos);
                // whitespace
                if (Character.isWhitespace(c)) { pos++; continue; }
                // line comment //...
                if (c == '/' && pos + 1 < s.length() && s.charAt(pos + 1) == '/') {
                    pos += 2;
                    while (!eof() && s.charAt(pos) != '\n') pos++;
                    continue;
                }
                // block comment /* ... */
                if (c == '/' && pos + 1 < s.length() && s.charAt(pos + 1) == '*') {
                    pos += 2;
                    while (pos + 1 < s.length() && !(s.charAt(pos) == '*' && s.charAt(pos + 1) == '/')) pos++;
                    if (pos + 1 < s.length()) pos += 2; // consume */
                    continue;
                }
                // hash comment to EOL
                if (c == '#') {
                    while (!eof() && s.charAt(pos) != '\n') pos++;
                    continue;
                }
                break;
            }
        }

        @Nullable
        String tryReadId() {
            int save = pos;
            try {
                String id = readId();
                return id;
            } catch (Exception e) {
                pos = save;
                return null;
            }
        }

        @Nullable
        String tryReadIdOrString() {
            skipWs();
            if (eof()) return null;
            char c0 = s.charAt(pos);
            if (c0 == '"') return readQuotedString();
            if (c0 == '<') return readAngleString();
            int save = pos;
            try {
                return readId();
            } catch (Exception e) {
                pos = save;
                return null;
            }
        }

    }

    /** "graph" or "digraph" */
    private static final String DOT_TYPE_KEY = "dot.type";
    public static final String SUBGRAPH = "subgraph";
    public static final char CURLY_BRACE_CLOSE = '}';
    public static final char CURLY_BRACE_OPEN = '{';
    public static final String NODE = "node";
    public static final String EDGE = "edge";
    private final StringBuilder buf = new StringBuilder();
    private final List<Integer> lineStarts = new ArrayList<>(); // 0-based start index of each input line in buf
    private final CjDocumentElement cjDocument;
    private boolean firstLine = true;
    private Parser currentParser = null;

    public DotLines2CjDocument(@Nullable Consumer<ContentError> contentErrorHandler) {
        super.setContentErrorHandler(contentErrorHandler);
        this.cjDocument = new CjDocumentElement();
    }

    private static void applyAttrsToHasDataAndLabel(List<Attr> attrs, ICjHasDataMutable hasData) {
        // if element supports label, set from 'label' attribute
        ICjHasLabelMutable labelTarget = (hasData instanceof ICjHasLabelMutable) ? (ICjHasLabelMutable) hasData : null;
        boolean isGraph = hasData instanceof ICjGraphMutable;
        for (Attr a : attrs) {
            if ("label".equalsIgnoreCase(a.key)) {
                if (isGraph) {
                    // For graphs, keep label as data attribute so emitter prints graph [label=...]
                    hasData.dataMutable(d -> d.addProperty(a.key, a.value));
                } else if (labelTarget != null) {
                    String val = a.value;
                    labelTarget.setLabel(l -> l.addEntry(le -> le.value(val)));
                } else {
                    hasData.dataMutable(d -> d.addProperty(a.key, a.value));
                }
            } else {
                hasData.dataMutable(d -> d.addProperty(a.key, a.value));
            }
        }
    }

    private static ICjNodeMutable createNode(ICjGraphMutable g, String id) {
        final ICjNodeMutable[] ref = new ICjNodeMutable[1];
        g.addNode(n -> {
            n.id(id);
            ref[0] = n;
        });
        return ref[0];
    }

    private static ICjGraphMutable createSubgraph(ICjGraphMutable parent, @Nullable String id) {
        final ICjGraphMutable[] ref = new ICjGraphMutable[1];
        parent.addGraph(gg -> {
            if (id != null) gg.id(id);
            ref[0] = gg;
        });
        return ref[0];
    }

    private static String stripComments(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '/') {
                // // comment
                i += 2;
                while (i < n && s.charAt(i) != '\n') i++;
            } else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                // /* */ comment
                i += 2;
                while (i + 1 < n && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) i++;
                i = Math.min(n, i + 2);
            } else if (c == '#') {
                // # comment to end of line
                while (i < n && s.charAt(i) != '\n') i++;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    @Override
    public void line(String line) {
        if (!firstLine) {
            buf.append('\n');
        }
        // record start offset for this incoming line
        lineStarts.add(buf.length());
        firstLine = false;
        buf.append(line);
    }

    private Location mapPosToLocation(int pos) {
        if (lineStarts.isEmpty()) return Location.UNAVAILABLE;
        int lineIdx = 0;
        for (int i = 0; i < lineStarts.size(); i++) {
            int start = lineStarts.get(i);
            if (start <= pos) lineIdx = i; else break;
        }
        int start = lineStarts.get(lineIdx);
        int col = (pos - start) + 1; // 1-based
        int lineNo = lineIdx + 1; // 1-based
        return new Location(lineNo, col);
    }

    public CjDocumentElement resultDocument() {
        String dot = buf.toString();
        Parser p = new Parser(dot);
        this.currentParser = p;
        // install dynamic locator tied to current parser position
        super.setLocator(() -> mapPosToLocation(currentParser != null ? currentParser.position() : dot.length()));
        try {
            TopLevel tl = p.parseTopLevelHeader();
            // Build document with a single top-level graph
            cjDocument.addGraph((ICjGraphMutable g) -> {
                if (tl.id != null) g.id(tl.id);
                g.dataMutable(d -> d.addProperty(DOT_TYPE_KEY, tl.directed ? "digraph" : "graph"));
                // parse graph body
                p.expect(CURLY_BRACE_OPEN);
                parseStatements(p, g, tl.directed);
                p.skipWs();
                p.expect(CURLY_BRACE_CLOSE);
            });
            return cjDocument;
        } catch (RuntimeException ex) {
            // Emit a content error with precise location
            int pos = currentParser != null ? currentParser.position() : dot.length();
            Location loc = mapPosToLocation(pos);
            String msg = ex.getMessage() == null ? (ex.getClass().getSimpleName()) : ex.getMessage();
            throw sendContentError_Error("DOT parse error at " + loc + ": " + msg, ex);
        }
    }

    private void parseStatements(Parser p, ICjGraphMutable g, boolean directed) {
        Map<String, ICjNodeMutable> nodesById = new LinkedHashMap<>();
        while (true) {
            p.skipWs();
            if (p.eof() || p.peek() == CURLY_BRACE_CLOSE) break;
            // Handle subgraph or anonymous block
            if (p.lookaheadKeyword(SUBGRAPH)) {
                p.consumeKeyword(SUBGRAPH);
                String subId = p.tryReadIdOrString();
                ICjGraphMutable sub = createSubgraph(g, subId);
                p.skipWs();
                if (p.peek() == CURLY_BRACE_OPEN) {
                    p.expect(CURLY_BRACE_OPEN);
                    parseStatements(p, sub, directed);
                    p.skipWs();
                    p.expect(CURLY_BRACE_CLOSE);
                }
                p.consumeOptionalSemicolon();
                continue;
            }
            if (p.peek() == CURLY_BRACE_OPEN) {
                // anonymous subgraph; parse but ignore contained standalone ids
                ICjGraphMutable sub = createSubgraph(g, null);
                p.expect(CURLY_BRACE_OPEN);
                parseStatements(p, sub, directed);
                p.skipWs();
                p.expect(CURLY_BRACE_CLOSE);
                p.consumeOptionalSemicolon();
                continue;
            }
            // Defaults like node [...] / edge [...] / graph [...] -> for graph, apply to current graph; others ignored
            if (p.lookaheadKeyword(Parser.GRAPH)) {
                p.readKeyword();
                p.skipWs();
                if (p.peek() == '[') {
                    List<Attr> attrs = p.readAttrList(null);
                    applyAttrsToHasDataAndLabel(attrs, g);
                }
                p.consumeOptionalSemicolon();
                continue;
            } else if (p.lookaheadKeyword(NODE) || p.lookaheadKeyword(EDGE)) {
                p.readKeyword();
                p.skipWs();
                if (p.peek() == '[') {
                    p.readAttrList(null); // ignore defaults for now
                }
                p.consumeOptionalSemicolon();
                continue;
            }
            // Assignment at graph level: id = value;
            int save = p.pos;
            String maybeKey = p.tryReadIdOrString();
            if (maybeKey != null) {
                p.skipWs();
                if (p.consumeIf('=')) {
                    String value = p.readIdOrString();
                    // store as graph-level data
                    g.dataMutable(d -> d.addProperty(maybeKey, value));
                    p.consumeOptionalSemicolon();
                    continue;
                } else {
                    // Could be node/edge starting with a nodeRef; rewind and parse stmt
                    p.pos = save;
                }
            }
            // Node or Edge statement
            NodeRef first = readNodeRef(p);
            p.skipWs();
            if (p.lookaheadEdgeOp()) {
                // Edge statement: parse as a sequence of segments, each segment may be a single nodeRef or a {group}
                List<List<NodeRef>> segments = new ArrayList<>();
                segments.add(List.of(first));
                String op = p.readEdgeOp(); // '--' or '->' (we rely on top-level directed flag)
                while (true) {
                    List<NodeRef> nextSeg = readNodeRefOrGroup(p);
                    segments.add(nextSeg);
                    p.skipWs();
                    if (p.lookaheadEdgeOp()) { p.readEdgeOp(); continue; }
                    break;
                }
                // zero or more attr lists following
                List<Attr> attrs = new ArrayList<>();
                while (true) {
                    p.skipWs();
                    if (!p.eof() && p.peek() == '[') {
                        attrs.addAll(p.readAttrList(null));
                        continue;
                    }
                    break;
                }
                // Emit edges for each adjacent pair of segments (DOT semantics)
                for (int i = 0; i + 1 < segments.size(); i++) {
                    List<NodeRef> left = segments.get(i);
                    List<NodeRef> right = segments.get(i + 1);
                    for (NodeRef a : left) {
                        for (NodeRef b : right) {
                            // ensure nodes exist
                            nodesById.computeIfAbsent(a.nodeId, id -> createNode(g, id));
                            nodesById.computeIfAbsent(b.nodeId, id -> createNode(g, id));
                            g.addEdge(e -> {
                                e.addEndpoint(ep -> {
                                    ep.node(a.nodeId);
                                    if (a.port != null) ep.port(a.port);
                                    ep.direction(CjDirection.UNDIR);
                                });
                                e.addEndpoint(ep -> {
                                    ep.node(b.nodeId);
                                    if (b.port != null) ep.port(b.port);
                                    ep.direction(CjDirection.UNDIR);
                                });
                                applyAttrsToHasDataAndLabel(attrs, e);
                            });
                        }
                    }
                }
                p.consumeOptionalSemicolon();
            } else {
                // Node statement with optional multiple attr lists
                List<Attr> attrs = new ArrayList<>();
                while (true) {
                    p.skipWs();
                    if (!p.eof() && p.peek() == '[') {
                        attrs.addAll(p.readAttrList(null));
                        continue;
                    }
                    break;
                }
                ICjNodeMutable node = nodesById.computeIfAbsent(first.nodeId, id -> createNode(g, id));
                applyAttrsToHasDataAndLabel(attrs, node);
                p.consumeOptionalSemicolon();
            }
        }
    }

    private NodeRef readNodeRef(Parser p) {
        String id = p.readIdOrString();
        String port = null;
        p.skipWs();
        if (p.consumeIf(':')) {
            port = p.readIdOrString();
            // optional compass after another ':' is ignored
            p.skipWs();
            if (p.consumeIf(':')) {
                p.readIdOrString();
            }
        }
        return new NodeRef(id, port);
    }

    /** Read either a single nodeRef or a group { a, b, c } returning a list of NodeRefs. */
    private List<NodeRef> readNodeRefOrGroup(Parser p) {
        p.skipWs();
        if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
            // group
            p.expect(CURLY_BRACE_OPEN);
            List<NodeRef> list = new ArrayList<>();
            while (true) {
                p.skipWs();
                if (!p.eof() && p.peek() == CURLY_BRACE_CLOSE) { p.expect(CURLY_BRACE_CLOSE); break; }
                // allow empty segments to be skipped
                NodeRef nr = readNodeRef(p);
                list.add(nr);
                // optional separators: comma or semicolon; also allow plain whitespace between ids
                while (true) {
                    p.skipWs();
                    if (p.consumeIf(',')) continue;
                    if (p.consumeIf(';')) continue;
                    break;
                }
            }
            return list;
        }
        return List.of(readNodeRef(p));
    }

}
