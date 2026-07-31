package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.text.ITextWriter;
import com.graphinout.foundation.pure.input.BaseOutput;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.jspecify.annotations.Nullable;

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

    private record Attr(String key, String value, boolean html) {}

    @SuppressWarnings("SameParameterValue")
    private static final class Parser {

        public static final String DIGRAPH = "digraph";
        public static final String GRAPH = "graph";
        private final String s;
        private int pos = 0;

        Parser(String s) {this.s = s;}

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
            if (!kw.equals(got)) throw new IllegalStateException("Expected keyword '" + kw + "' but got '" + got + "'");
        }

        /**
         * Consume the optional separator that may follow a statement. A {@code ';'} genuinely separates
         * statements. A {@code ','} does not: in DOT a comma only chains node ids into a nodelist
         * ({@code a, b -> c}), so whatever follows it has to be another node id.
         */
        void consumeOptionalSemicolon() {
            skipWs();
            if (eof()) return;
            char c = s.charAt(pos);
            if (c == ';') {
                pos++;
            } else if (c == ',') {
                pos++;
                requireNodeIdAfterComma();
            }
        }

        /**
         * Consume separators between the elements of an endpoint group / nodelist, applying the same
         * comma rule as {@link #consumeOptionalSemicolon()}.
         */
        void consumeGroupSeparators() {
            while (true) {
                skipWs();
                if (eof()) break;
                char c = s.charAt(pos);
                if (c == ',') {
                    pos++;
                    requireNodeIdAfterComma();
                    continue;
                }
                if (c == ';') {
                    pos++;
                    continue;
                }
                break;
            }
        }

        /**
         * A comma chains node ids ({@code a, b}); it is not a general separator. Reject the constructs
         * Graphviz rejects after one — {@code A -> } &#123;{@code B, subgraph } &#123;{@code C; D} &#125;&#125;
         * is "syntax error near 'subgraph'".
         */
        void requireNodeIdAfterComma() {
            int commaPos = pos - 1;
            skipWs();
            if (eof()) throw new IllegalStateException(
                    "Unexpected end of input after ',' at pos " + commaPos + ": a comma chains node ids, so it must be followed by a node id");
            char c = s.charAt(pos);
            if (c == CURLY_BRACE_OPEN || c == CURLY_BRACE_CLOSE || c == ';' || c == ',' || c == '[' || c == ']' || c == '=') {
                throw new IllegalStateException("Unexpected '" + c + "' at pos " + pos + " after the ',' at pos "
                        + commaPos + ": a comma chains node ids (as in 'a, b -> c'), so it must be followed by a node id");
            }
            if (lookaheadKeyword(SUBGRAPH)) {
                throw new IllegalStateException("Unexpected keyword '" + SUBGRAPH + "' at pos " + pos
                        + " after the ',' at pos " + commaPos
                        + ": a comma chains node ids (as in 'a, b -> c'), so it must be followed by a node id");
            }
        }

        /**
         * An {@code attr_list} terminates the statement it belongs to. DOT's grammar is
         * {@code node_stmt : node_id [attr_list]} and {@code edge_stmt : (node_id|subgraph) edgeRHS [attr_list]},
         * so a {@code ']'} can be followed by the next statement but never by another edge operator. This
         * rejects {@code A -> B [color=red] -> C} and {@code A [label="x"] -> B}, both of which Graphviz
         * reports as "syntax error near '->'".
         */
        void expectNoEdgeOpAfterAttrList(String stmtKind) {
            skipWs();
            if (!eof() && (match("->") || match("--"))) {
                String op = s.substring(pos, Math.min(pos + 2, s.length()));
                throw new IllegalStateException("Unexpected edge operator '" + op + "' at pos " + pos
                        + ": an attribute list terminates the enclosing " + stmtKind + " statement, so '" + op
                        + "' cannot continue it");
            }
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
                default -> throw new IllegalStateException("Expected " + GRAPH + " or " + DIGRAPH + ", got " + kind);
            };
            String id = tryReadIdOrString();
            return new TopLevel(directed, id);
        }

        char peek() {return s.charAt(pos);}

        int position() {return pos;}

        String readAngleString() {
            skipWs();
            if (s.charAt(pos) != '<') throw new IllegalStateException("Expected '<' at pos " + pos);
            int start = pos;
            StringBuilder out = new StringBuilder();
            // consume the outer opener
            out.append(s.charAt(pos++));
            int tagDepth = 0; // number of open inner tags (<tag> not yet closed)
            while (!eof()) {
                char c = s.charAt(pos);
                if (c == '<') {
                    // Parse a tag: <...>
                    int tagStart = pos;
                    // Determine if this is closing or opening/self-closing
                    pos++; // consume '<'
                    out.append('<');
                    boolean closing = false;
                    if (!eof() && s.charAt(pos) == '/') {
                        closing = true;
                        out.append('/');
                        pos++;
                    }
                    // read until '>'
                    boolean selfClosing = false;
                    while (!eof()) {
                        char t = s.charAt(pos++);
                        out.append(t);
                        if (t == '>') {
                            // end of tag
                            if (!closing) {
                                // check if previous char before '>' was '/'
                                int prev = pos - 2;
                                if (prev >= 0 && s.charAt(prev) == '/') selfClosing = true;
                            }
                            break;
                        }
                    }
                    if (!closing && !selfClosing) tagDepth++;
                    if (closing && tagDepth > 0) tagDepth--;
                    continue;
                }
                if (c == '>') {
                    // End of HTML string only if there are no open tags
                    if (tagDepth == 0) {
                        out.append('>');
                        pos++;
                        break;
                    } else {
                        // treat as plain text inside a tag context
                        out.append('>');
                        pos++;
                        continue;
                    }
                }
                // regular content character
                out.append(c);
                pos++;
            }
            if (out.isEmpty() || out.charAt(out.length() - 1) != '>') {
                throw new IllegalStateException("Unterminated HTML-like string starting at pos " + start);
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
                    boolean isHtml = val.startsWith("<");
                    list.add(new Attr(key, val, isHtml));
                } else {
                    // attributes without value -> store as key=true
                    list.add(new Attr(key, "true", false));
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
                    while (!eof() && Character.isDigit(s.charAt(pos))) {
                        pos++;
                        sawDigit = true;
                    }
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
            return readId();
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
                if (Character.isWhitespace(c)) {
                    pos++;
                    continue;
                }
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

        @Nullable String tryReadId() {
            int save = pos;
            try {
                return readId();
            } catch (Exception e) {
                pos = save;
                return null;
            }
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
                pos = save;
                return null;
            }
        }

    }

    public static final String SUBGRAPH = "subgraph";
    public static final char CURLY_BRACE_CLOSE = '}';
    public static final char CURLY_BRACE_OPEN = '{';
    public static final String NODE = "node";
    public static final String EDGE = "edge";
    /** "graph" or "digraph" */
    private static final String DOT_TYPE_KEY = "dot.type";
    private final StringBuilder buf = new StringBuilder();
    private final List<Integer> lineStarts = new ArrayList<>(); // 0-based start index of each input line in buf
    private final CjDocumentElement cjDocument;
    private boolean firstLine = true;
    private Parser currentParser = null;

    private final int syntheticIdCounter = 1;

    public DotLines2CjDocument(@Nullable Consumer<ContentError> contentErrorHandler) {
        super.setContentErrorHandler(contentErrorHandler);
        this.cjDocument = new CjDocumentElement();
    }

    private static void applyAttrsToHasDataAndLabel(List<Attr> attrs, ICjHasDataMutable hasData) {
        // if element supports label, set from 'label' attribute
        ICjHasLabelMutable labelTarget = (hasData instanceof ICjHasLabelMutable) ? (ICjHasLabelMutable) hasData : null;
        boolean isGraph = hasData instanceof ICjGraphMutable;
        // Use LinkedHashMap to preserve order while ensuring last value wins for duplicate keys
        Map<String, Attr> uniqueAttrs = new LinkedHashMap<>();
        for (Attr a : attrs) {
            uniqueAttrs.put(a.key, a);
        }
        for (Attr a : uniqueAttrs.values()) {
            if ("label".equalsIgnoreCase(a.key)) {
                if (isGraph) {
                    // For graphs, keep label as data attribute so emitter prints graph [label=...]
                    putAttr(hasData, a);
                } else if (labelTarget != null) {
                    String val = a.value;
                    labelTarget.setLabel(l -> l.addEntry(le -> le.value(val)));
                } else {
                    putAttr(hasData, a);
                }
            } else {
                putAttr(hasData, a);
            }
        }
    }

    /**
     * Set a single attribute as data, overwriting any existing value for the same key. Re-declaring an element
     * (e.g. {@code insert [color="..."]} twice) overrides per DOT semantics rather than accumulating into an
     * array, which the emitter could not serialize (issue #133).
     */
    private static void putAttr(ICjHasDataMutable hasData, Attr a) {
        if (a.html) {
            hasData.dataMutable(m -> {
                if (m.jsonValue() != null) m.remove(a.key);
                IJsonObjectMutable o = m.factory().createObjectMutable();
                o.addProperty("type", m.factory().createString("html"));
                // strip outer << >> if present
                String v = a.value;
                if (v.startsWith("<<") && v.endsWith(">>")) v = v.substring(2, v.length() - 2);
                o.addProperty("value", m.factory().createString(v));
                m.add(a.key, o);
            });
        } else {
            hasData.dataMutable(d -> {
                if (d.jsonValue() != null) d.remove(a.key);
                d.add(a.key, a.value);
            });
        }
    }

    /**
     * Emit one CJ edge for a DOT edge from {@code a} to {@code b}. In a digraph (directed), the left endpoint is the
     * source (direction {@code in}) and the right endpoint is the target (direction {@code out}), matching the CJ
     * convention. In an undirected graph both endpoints are {@code undir}.
     */
    private static void addEdge(ICjGraphMutable g, NodeRef a, NodeRef b, boolean directed, List<Attr> attrsAccum) {
        g.addEdge(e -> {
            e.addEndpoint(ep -> {
                ep.node(a.nodeId);
                if (a.port != null) ep.port(a.port);
                ep.direction(directed ? CjDirection.IN : CjDirection.UNDIR);
            });
            e.addEndpoint(ep -> {
                ep.node(b.nodeId);
                if (b.port != null) ep.port(b.port);
                ep.direction(directed ? CjDirection.OUT : CjDirection.UNDIR);
            });
            applyAttrsToHasDataAndLabel(attrsAccum, e);
        });
    }

    private static ICjNodeMutable createNode(ICjGraphMutable g, String id) {
        final ICjNodeMutable[] ref = new ICjNodeMutable[1];
        g.addNode(n -> {
            n.id(id);
            ref[0] = n;
        });
        return ref[0];
    }

    private record NodeCluster(String nodeId, String innerId) {}

    /**
     * Recognise a DOT subgraph that encodes a node's nested graph (written by {@code CjDocument2Dot}). The cluster name
     * has the shape {@code cluster_nodegraph__<nodeId>__graph__<innerId>}.
     *
     * @return the owning node id and inner graph id, or {@code null} if this is an ordinary subgraph
     */
    private static @Nullable NodeCluster parseNodeCluster(@Nullable String subId) {
        if (subId == null || !subId.startsWith(CjDocument2Dot.NODE_CLUSTER_PREFIX)) return null;
        String rest = subId.substring(CjDocument2Dot.NODE_CLUSTER_PREFIX.length());
        int sep = rest.indexOf(CjDocument2Dot.NODE_CLUSTER_SEP);
        if (sep < 0) return null;
        String nodeId = rest.substring(0, sep);
        String innerId = rest.substring(sep + CjDocument2Dot.NODE_CLUSTER_SEP.length());
        if (nodeId.isEmpty()) return null;
        return new NodeCluster(nodeId, innerId);
    }

    private static ICjGraphMutable createNodeSubgraph(ICjNodeMutable owner, @Nullable String id) {
        final ICjGraphMutable[] ref = new ICjGraphMutable[1];
        owner.addGraph(gg -> {
            if (id != null) gg.id(id);
            ref[0] = gg;
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

    /**
     * This one triggers the actual parse.
     *
     * @return
     */
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
                g.dataMutable(d -> d.add(DOT_TYPE_KEY, tl.directed ? "digraph" : "graph"));
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
            throw sendContentError_Error("DOT parse error", ex, () -> mapPosToLocation(pos));
        }
    }

    private Location mapPosToLocation(int pos) {
        if (lineStarts.isEmpty()) return Location.UNAVAILABLE;
        int lineIdx = 0;
        for (int i = 0; i < lineStarts.size(); i++) {
            int start = lineStarts.get(i);
            if (start <= pos) lineIdx = i;
            else break;
        }
        int start = lineStarts.get(lineIdx);
        int col = (pos - start) + 1; // 1-based
        int lineNo = lineIdx + 1; // 1-based
        return new Location(lineNo, col);
    }

    private void parseStatements(Parser p, ICjGraphMutable g, boolean directed) {
        Map<String, ICjNodeMutable> nodesById = new LinkedHashMap<>();
        while (true) {
            p.skipWs();
            if (p.eof() || p.peek() == CURLY_BRACE_CLOSE) break;
            // Handle possible edge starting with an anonymous subgraph/group: "{ ... } -> ..."
            if (p.peek() == CURLY_BRACE_OPEN) {
                int savePos = p.position();
                // Look ahead to see if this is a pure node-id group followed by an edge operator
                int i = savePos + 1; // after '{'
                int depth = 1;
                boolean hasEqualsInside = false;
                while (i < p.s.length() && depth > 0) {
                    char c = p.s.charAt(i++);
                    if (c == '{') depth++;
                    else if (c == '}') depth--;
                    else if (c == '=') hasEqualsInside = true;
                }
                // i now points just after the matching '}' (or EOF)
                int afterGroup = i;
                // Skip whitespace/comments using parser helper: temporarily set pos
                int oldPos = p.pos;
                p.pos = afterGroup;
                p.skipWs();
                boolean edgeFollows = p.lookaheadEdgeOp();
                p.pos = oldPos;
                if (edgeFollows && !hasEqualsInside) {
                    // Treat as endpoint group of ids and parse edge chain
                    List<NodeRef> firstSeg = readNodeRefOrGroup(p, g, directed, nodesById);
                    parseEdgeChainTail(p, g, directed, nodesById, firstSeg, new ArrayList<>());
                    continue;
                } else {
                    // Anonymous subgraph block
                    ICjGraphMutable sub = createSubgraph(g, null);
                    p.expect(CURLY_BRACE_OPEN);
                    parseStatements(p, sub, directed);
                    p.skipWs();
                    p.expect(CURLY_BRACE_CLOSE);
                    p.consumeOptionalSemicolon();
                    continue;
                }
            }
            // Handle edge starting with 'subgraph ... { ... } ->'
            if (p.lookaheadKeyword(SUBGRAPH)) {
                int savePos = p.position();
                p.consumeKeyword(SUBGRAPH);
                p.tryReadIdOrString();
                p.skipWs();
                boolean edgeAfterBody = false;
                if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
                    // scan to matching '}'
                    int i2 = p.position();
                    int depth2 = 0;
                    while (i2 < p.s.length()) {
                        char c2 = p.s.charAt(i2++);
                        if (c2 == '{') depth2++;
                        else if (c2 == '}') {
                            depth2--;
                            if (depth2 == 0) break;
                        }
                    }
                    int old = p.pos;
                    p.pos = i2;
                    p.skipWs();
                    edgeAfterBody = p.lookaheadEdgeOp();
                }
                // reset
                p.pos = savePos;
                if (edgeAfterBody) {
                    // Parse as edge chain starting with a subgraph endpoint
                    List<NodeRef> firstSeg = readNodeRefOrGroup(p, g, directed, nodesById);
                    parseEdgeChainTail(p, g, directed, nodesById, firstSeg, new ArrayList<>());
                    continue;
                }
            }
            // Handle subgraph or anonymous block starting with keyword
            if (p.lookaheadKeyword(SUBGRAPH)) {
                // Only treat as subgraph statement if followed by optional id and a '{' body.
                int savePos = p.position();
                p.consumeKeyword(SUBGRAPH);
                String subId = p.tryReadIdOrString();
                p.skipWs();
                if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
                    NodeCluster nc = parseNodeCluster(subId);
                    ICjGraphMutable sub;
                    if (nc != null) {
                        // nested-graphs-in-nodes: attach this subgraph to the owning node, not to the parent graph
                        ICjNodeMutable owner = nodesById.computeIfAbsent(nc.nodeId, id -> createNode(g, id));
                        sub = createNodeSubgraph(owner, nc.innerId.isEmpty() ? null : nc.innerId);
                    } else {
                        sub = createSubgraph(g, subId);
                    }
                    p.expect(CURLY_BRACE_OPEN);
                    parseStatements(p, sub, directed);
                    p.skipWs();
                    p.expect(CURLY_BRACE_CLOSE);
                    p.consumeOptionalSemicolon();
                    continue;
                } else {
                    // Not a real subgraph statement (likely a node named 'subgraph'); rewind and let normal parsing handle it
                    // restore position to before keyword
                    // Note: Parser.position() returns pos; we need to set pos back using reflection, but Parser has field pos; accessible here.
                    // We emulate rewind by directly assigning p.pos
                    //noinspection AccessingNonPublicFieldOfAnotherObject
                    p.pos = savePos;
                }
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
                    // store as graph-level data (remove first to overwrite if key already exists)
                    g.dataMutable(d -> {
                        if (d.jsonValue() != null) {
                            d.remove(maybeKey);
                        }
                        d.add(maybeKey, value);
                    });
                    p.consumeOptionalSemicolon();
                    continue;
                } else {
                    // Could be node/edge starting with a nodeRef; rewind and parse stmt
                    p.pos = save;
                }
            }
            // Node or Edge statement
            NodeRef first = readNodeRef(p);
            // Collect the attr_list(s) that may directly follow the first node id. `a [x=1][y=2]` is one
            // node_stmt; an attr_list here CLOSES the statement, so `A [label="x"] -> B` is malformed.
            List<Attr> attrs = new ArrayList<>();
            boolean sawAttrList = false;
            while (true) {
                p.skipWs();
                if (!p.eof() && p.peek() == '[') {
                    attrs.addAll(p.readAttrList(null));
                    sawAttrList = true;
                    continue;
                }
                break;
            }
            if (sawAttrList) p.expectNoEdgeOpAfterAttrList("node");
            p.skipWs();
            if (p.lookaheadEdgeOp()) {
                // Edge statement: a sequence of segments, each a single nodeRef or a {group}.
                // `attrs` is provably empty here — an attr_list before the edgeop was rejected above.
                parseEdgeChainTail(p, g, directed, nodesById, List.of(first), attrs);
            } else {
                // Node statement. Its attr_list was consumed above, so apply it here (issue #133).
                ICjNodeMutable node = nodesById.computeIfAbsent(first.nodeId, id -> createNode(g, id));
                applyAttrsToHasDataAndLabel(attrs, node);
                p.consumeOptionalSemicolon();
            }
        }
    }

    /**
     * Parse the rest of an edge statement whose first endpoint has already been read, then emit one CJ edge per
     * adjacent endpoint pair and consume the statement separator.
     * <p>
     * DOT: {@code edge_stmt : (node_id | subgraph) edgeRHS [attr_list]} — the {@code attr_list} comes LAST and
     * terminates the statement, so an edge operator may never follow it. That is enforced here, which is what
     * makes {@code A -> B [color=red] -> C} the syntax error Graphviz says it is.
     *
     * @param firstSeg the endpoint already parsed (a single node id, a group, or a subgraph)
     * @param attrs    attributes collected before the chain; empty for a well-formed statement
     */
    private void parseEdgeChainTail(Parser p, ICjGraphMutable g, boolean directed,
            Map<String, ICjNodeMutable> nodesById, List<NodeRef> firstSeg, List<Attr> attrs) {
        List<List<NodeRef>> segments = new ArrayList<>();
        segments.add(firstSeg);
        while (true) {
            p.skipWs();
            if (!p.lookaheadEdgeOp()) break;
            p.readEdgeOp();
            segments.add(readNodeRefOrGroup(p, g, directed, nodesById));
        }
        boolean sawAttrList = false;
        while (true) {
            p.skipWs();
            if (!p.eof() && p.peek() == '[') {
                attrs.addAll(p.readAttrList(null));
                sawAttrList = true;
                continue;
            }
            break;
        }
        if (sawAttrList) p.expectNoEdgeOpAfterAttrList("edge");
        for (int i = 0; i + 1 < segments.size(); i++) {
            List<NodeRef> left = segments.get(i);
            List<NodeRef> right = segments.get(i + 1);
            for (NodeRef a : left) {
                for (NodeRef b : right) {
                    // ensure nodes exist
                    nodesById.computeIfAbsent(a.nodeId, id -> createNode(g, id));
                    nodesById.computeIfAbsent(b.nodeId, id -> createNode(g, id));
                    addEdge(g, a, b, directed, attrs);
                }
            }
        }
        p.consumeOptionalSemicolon();
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

    /**
     * Read either a single nodeRef, a 'subgraph' endpoint with optional body, or a group { a, b, c }. If a 'subgraph'
     * keyword is used as an endpoint followed by an optional id and/or body, we create that subgraph on the current
     * graph and return a single NodeRef to a node literally named 'subgraph' to preserve DOT round-trip.
     */
    private List<NodeRef> readNodeRefOrGroup(Parser p, ICjGraphMutable g, boolean directed, Map<String, ICjNodeMutable> nodesById) {
        p.skipWs();
        // Special: 'subgraph' as endpoint in an edge chain
        if (p.lookaheadKeyword(SUBGRAPH)) {
            p.consumeKeyword(SUBGRAPH);
            String subId = p.tryReadIdOrString();
            // Create a nested subgraph and return all node refs inside it to expand edges per DOT spec
            final ICjGraphMutable[] nestedRef = new ICjGraphMutable[1];
            // Attach the subgraph to the current graph
            g.addGraph(gg -> {
                if (subId != null) gg.id(subId);
                nestedRef[0] = gg;
            });
            p.skipWs();
            if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
                p.expect(CURLY_BRACE_OPEN);
                parseStatements(p, nestedRef[0], directed);
                p.skipWs();
                p.expect(CURLY_BRACE_CLOSE);
            }
            // Collect node ids inside the subgraph to expand the edge
            List<NodeRef> result = new ArrayList<>();
            nestedRef[0].nodes().forEach(n -> result.add(new NodeRef(n.id(), null)));
            return result;
        }
        if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
            // group
            p.expect(CURLY_BRACE_OPEN);
            List<NodeRef> list = new ArrayList<>();
            while (true) {
                p.skipWs();
                if (!p.eof() && p.peek() == CURLY_BRACE_CLOSE) {
                    p.expect(CURLY_BRACE_CLOSE);
                    break;
                }
                // Allow and ignore simple assignments like rank=same inside a group
                int savePos = p.position();
                // Allow nested subgraph inside an endpoint group: expand to the subgraph's nodes per DOT spec
                if (p.lookaheadKeyword(SUBGRAPH)) {
                    p.consumeKeyword(SUBGRAPH);
                    String subId = p.tryReadIdOrString();
                    final ICjGraphMutable[] nestedRef = new ICjGraphMutable[1];
                    g.addGraph(gg -> {
                        if (subId != null) gg.id(subId);
                        nestedRef[0] = gg;
                    });
                    p.skipWs();
                    if (!p.eof() && p.peek() == CURLY_BRACE_OPEN) {
                        p.expect(CURLY_BRACE_OPEN);
                        parseStatements(p, nestedRef[0], directed);
                        p.skipWs();
                        p.expect(CURLY_BRACE_CLOSE);
                    }
                    // add all nodes from subgraph to the group list
                    nestedRef[0].nodes().forEach(n -> list.add(new NodeRef(n.id(), null)));
                    // optional separators after subgraph block
                    p.consumeGroupSeparators();
                    continue;
                }
                String maybeKey = p.tryReadIdOrString();
                if (maybeKey != null) {
                    p.skipWs();
                    if (p.consumeIf('=')) {
                        p.readIdOrString();
                        // consume separators after assignment
                        p.consumeGroupSeparators();
                        continue; // do not add a node for assignments
                    } else {
                        // not an assignment; rewind and read a node ref
                        p.pos = savePos;
                    }
                }
                NodeRef nr = readNodeRef(p);
                list.add(nr);
                // optional separators: comma or semicolon; also allow plain whitespace between ids
                p.consumeGroupSeparators();
            }
            return list;
        }
        return List.of(readNodeRef(p));
    }

}
