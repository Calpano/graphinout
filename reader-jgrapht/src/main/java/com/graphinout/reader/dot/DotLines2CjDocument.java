package com.graphinout.reader.dot;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.element.*;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.foundation.text.ITextWriter;

import javax.annotation.Nullable;
import java.util.*;

/**
 * DOT → CJ parser.
 * Implements a practical subset of the DOT grammar sufficient for the bundled tests:
 * - [strict]? (di)graph [id]? '{' stmt_list '}'
 * - stmt: node stmt, edge chain, subgraph (named/anonymous), assignment (id '=' id), attr lists [k=v,...]
 * - default attribute statements (node[..], edge[..], graph[..]) and rank groups are parsed leniently but ignored
 * - comments (//, /* * /, #) are stripped
 *
 * Mapping used:
 * - graph/subgraph → ICjGraph (nested)
 * - node → ICjNode
 * - edge chain → ICjEdge with ordered endpoints (direction left unspecified)
 * - attr_list → CjData on the corresponding element; label → ICjLabel (not duplicated in data)
 * - port → endpoint.port (compass point, if present, is ignored)
 */
public class DotLines2CjDocument implements ITextWriter {

    private static final String DOT_TYPE_KEY = "dot.type"; // "graph" or "digraph"

    private final StringBuilder buf = new StringBuilder();

    public CjDocumentElement resultDocument() {
        String dot = buf.toString();
        Parser p = new Parser(stripComments(dot));
        TopLevel tl = p.parseTopLevelHeader();
        // Build document with a single top-level graph
        cjDocument.addGraph((ICjGraphMutable g) -> {
            if (tl.id != null) g.id(tl.id);
            g.dataMutable(d -> d.addProperty(DOT_TYPE_KEY, tl.directed ? "digraph" : "graph"));
            // parse graph body
            p.expect('{');
            parseStatements(p, g, tl.directed);
            p.skipWs();
            p.expect('}');
        });
        return cjDocument;
    }

    private void parseStatements(Parser p, ICjGraphMutable g, boolean directed) {
        Map<String, ICjNodeMutable> nodesById = new LinkedHashMap<>();
        while (true) {
            p.skipWs();
            if (p.eof() || p.peek() == '}') break;
            // Handle subgraph or anonymous block
            if (p.lookaheadKeyword("subgraph")) {
                p.consumeKeyword("subgraph");
                String subId = p.tryReadIdOrString();
                ICjGraphMutable sub = createSubgraph(g, subId);
                p.skipWs();
                if (p.peek() == '{') {
                    p.expect('{');
                    parseStatements(p, sub, directed);
                    p.skipWs();
                    p.expect('}');
                }
                p.consumeOptionalSemicolon();
                continue;
            }
            if (p.peek() == '{') {
                // anonymous subgraph; parse but ignore contained standalone ids
                ICjGraphMutable sub = createSubgraph(g, null);
                p.expect('{');
                parseStatements(p, sub, directed);
                p.skipWs();
                p.expect('}');
                p.consumeOptionalSemicolon();
                continue;
            }
            // Defaults like node [...] / edge [...] / graph [...] -> for graph, apply to current graph; others ignored
            if (p.lookaheadKeyword("graph")) {
                p.readKeyword();
                p.skipWs();
                if (p.peek() == '[') {
                    List<Attr> attrs = p.readAttrList(null);
                    applyAttrsToHasDataAndLabel(attrs, g);
                }
                p.consumeOptionalSemicolon();
                continue;
            } else if (p.lookaheadKeyword("node") || p.lookaheadKeyword("edge")) {
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
                // Edge chain
                List<NodeRef> chain = new ArrayList<>();
                chain.add(first);
                String op = p.readEdgeOp(); // ignore value, we know graph directedness
                while (true) {
                    NodeRef next = readNodeRef(p);
                    chain.add(next);
                    p.skipWs();
                    if (p.lookaheadEdgeOp()) {
                        p.readEdgeOp();
                        continue;
                    }
                    break;
                }
                // optional attr list
                List<Attr> attrs = p.peek() == '[' ? p.readAttrList(null) : Collections.emptyList();
                // build one CJ edge with endpoints in order
                g.addEdge(e -> {
                    for (NodeRef nr : chain) {
                        e.addEndpoint(ep -> {
                            ep.node(nr.nodeId);
                            if (nr.port != null) ep.port(nr.port);
                            // leave direction as UNDIR; digraph is indicated at graph level
                            ep.direction(CjDirection.UNDIR);
                        });
                    }
                    applyAttrsToHasDataAndLabel(attrs, e);
                });
                p.consumeOptionalSemicolon();
            } else {
                // Node statement with optional attr list
                List<Attr> attrs = p.peek() == '[' ? p.readAttrList(null) : Collections.emptyList();
                ICjNodeMutable node = nodesById.computeIfAbsent(first.nodeId, id -> createNode(g, id));
                applyAttrsToHasDataAndLabel(attrs, node);
                p.consumeOptionalSemicolon();
            }
        }
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
        g.addNode(n -> { n.id(id); ref[0] = n; });
        return ref[0];
    }

    private static ICjGraphMutable createSubgraph(ICjGraphMutable parent, @Nullable String id) {
        final ICjGraphMutable[] ref = new ICjGraphMutable[1];
        parent.addGraph(gg -> { if (id != null) gg.id(id); ref[0] = gg; });
        return ref[0];
    }

    private record TopLevel(boolean directed, @Nullable String id) {}
    private record NodeRef(String nodeId, @Nullable String port) {}
    private record Attr(String key, String value) {}

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

    private static String stripComments(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0; int n = s.length();
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

    private static final class Parser {
        private final String s;
        private int pos = 0;
        Parser(String s) { this.s = s; }
        boolean eof() { return pos >= s.length(); }
        char peek() { return s.charAt(pos); }
        void skipWs() {
            while (!eof()) {
                char c = s.charAt(pos);
                if (Character.isWhitespace(c)) { pos++; continue; }
                break;
            }
        }
        void expect(char ch) {
            skipWs();
            if (eof() || s.charAt(pos) != ch) throw new IllegalStateException("Expected '" + ch + "' at pos " + pos);
            pos++;
        }
        boolean consumeIf(char ch) {
            skipWs();
            if (!eof() && s.charAt(pos) == ch) { pos++; return true; }
            return false;
        }
        void consumeOptionalSemicolon() {
            skipWs();
            if (!eof() && (s.charAt(pos) == ';' || s.charAt(pos) == ',')) pos++;
        }
        boolean lookaheadKeyword(String kw) {
            skipWs();
            int save = pos;
            String id = tryReadId();
            boolean ok = kw.equals(id);
            pos = save;
            return ok;
        }
        void consumeKeyword(String kw) {
            String got = readKeyword();
            if (!kw.equals(got)) throw new IllegalStateException("Expected keyword '" + kw + "' but got '" + got + "'");
        }
        String readKeyword() {
            String id = readId();
            return id;
        }
        String readEdgeOp() {
            skipWs();
            if (match("->")) { pos += 2; return "->"; }
            if (match("--")) { pos += 2; return "--"; }
            throw new IllegalStateException("Expected edge operator at pos " + pos);
        }
        boolean lookaheadEdgeOp() {
            skipWs();
            return match("->") || match("--");
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
                case "digraph" -> true; case "graph" -> false; default -> throw new IllegalStateException("Expected graph/digraph, got " + kind);
            };
            String id = tryReadIdOrString();
            return new TopLevel(directed, id);
        }
        List<Attr> readAttrList(@Nullable List<Attr> reuse) {
            skipWs();
            if (peek() != '[') return Collections.emptyList();
            expect('[');
            List<Attr> list = reuse != null ? reuse : new ArrayList<>();
            while (true) {
                skipWs();
                if (!eof() && s.charAt(pos) == ']') { pos++; break; }
                String key = readIdOrString();
                skipWs();
                if (consumeIf('=')) {
                    String val = readIdOrString();
                    list.add(new Attr(key, val));
                } else {
                    // attributes without value -> store as key=true
                    list.add(new Attr(key, "true"));
                }
                skipWs();
                if (consumeIf(',')) continue;
            }
            return list;
        }
        String readId() {
            skipWs();
            if (eof()) throw new IllegalStateException("Unexpected EOF reading id");
            int start = pos;
            char c = s.charAt(pos);
            if (Character.isLetter(c) || c == '_' ) {
                pos++;
                while (!eof()) {
                    char d = s.charAt(pos);
                    if (Character.isLetterOrDigit(d) || d == '_') pos++; else break;
                }
                return s.substring(start, pos);
            }
            // number (integer or simple float like 1.5)
            if (Character.isDigit(c)) {
                pos++;
                while (!eof() && Character.isDigit(s.charAt(pos))) pos++;
                // optional decimal part
                if (!eof() && s.charAt(pos) == '.') {
                    int save = pos;
                    pos++;
                    if (!eof() && Character.isDigit(s.charAt(pos))) {
                        while (!eof() && Character.isDigit(s.charAt(pos))) pos++;
                    } else {
                        // lone dot, revert
                        pos = save;
                    }
                }
                return s.substring(start, pos);
            }
            throw new IllegalStateException("Invalid id start at pos " + pos);
        }
        @Nullable String tryReadId() {
            int save = pos;
            try {
                String id = readId();
                return id;
            } catch (Exception e) {
                pos = save; return null;
            }
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
        @Nullable String tryReadIdOrString() {
            skipWs();
            if (eof()) return null;
            char c0 = s.charAt(pos);
            if (c0 == '"') return readQuotedString();
            if (c0 == '<') return readAngleString();
            int save = pos;
            try {
                return readId();
            } catch (Exception e) {
                pos = save; return null;
            }
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
    }

    private final CjDocumentElement cjDocument;
    private boolean firstLine = true;

    public DotLines2CjDocument() {
        this.cjDocument = new CjDocumentElement();
    }

    @Override
    public void line(String line) {
        if (!firstLine) {
            buf.append('\n');
        }
        firstLine = false;
        buf.append(line);
    }

}
