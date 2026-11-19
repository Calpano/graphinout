package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.text.ITextWriter;
import com.graphinout.foundation.pure.input.BaseOutput;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * DOT → CJ parser. Implements a practical subset of the DOT grammar sufficient for the bundled tests:
 * <li>[strict]? (di)graph [id]? '{' stmt_list '}'</li>
 * <li>stmt: node stmt, edge chain, subgraph (named/anonymous), assignment (id '=' id), attr lists [k=v,...]</li>
 * <li>default attribute statements (node[..], edge[..], graph[..]) and rank groups are parsed leniently. TODO
 * node/edge defaults are not copied into data; they are only used to resolve ports ({@code shape}, {@code label},
 * {@code headport}, {@code tailport})</li>
 * <li>TODO comments (//, /* * /, #) are stripped</li>
 * <p>
 * Mapping used:
 * <ul>
 * <li>graph/subgraph → ICjGraph (nested)</li>
 * <li>node → ICjNode</li>
 * <li>edge chain → ICjEdge with ordered endpoints (TODO direction left unspecified)</li>
 * <li>attr_list → CjData on the corresponding element; label → ICjLabel (not duplicated in data)</li>
 * <li>ports: see below</li>
 * </ul>
 * <p>
 * Ports follow Graphviz semantics (see {@link DotPorts}), resolved after the whole document is parsed, since a node
 * may be declared after an edge uses its port:
 * <ul>
 * <li>Ports a node declares (fields {@code <p>} of a {@code record}/{@code Mrecord} label, {@code PORT="p"} in an
 * HTML label) → {@link com.graphinout.base.cj.document.ICjPort} on the node, id = port name.</li>
 * <li>A compass point is a built-in port: {@code A:n} → port {@code n} on {@code A}, {@code A:p:e} → port
 * {@code p:e} nested in port {@code p}. Both carry data {@code dot.compass}. Only compass points that are used are
 * added. A declared port wins over a compass point of the same name.</li>
 * <li>{@code A:p}, the {@code tailport}/{@code headport} edge attributes and {@code edge [tailport=…]} defaults →
 * {@code endpoint.port}. As in Graphviz, the attribute overrides the port written on the node id.</li>
 * <li>A port that is neither declared nor a compass point is ignored, as Graphviz does: it is reported as a warning
 * and kept only in endpoint data ({@code dot.port}, {@code dot.compass}), so the DOT writer can write it back.</li>
 * <li>If subgraphs repeat a node (so it becomes several CJ nodes), only the one whose statement set the label gets
 * the ports. If its record shape came from {@code node [shape=…]} defaults, that shape is copied into its data, since
 * the ports depend on it.</li>
 * </ul>
 */
public class DotLines2CjDocument extends BaseOutput implements ITextWriter {

    private record TopLevel(boolean directed, @Nullable String id) {}

    /**
     * @param port    the port name after the first ':', or null
     * @param compass the compass point after the second ':', or null
     * @param pos     input position of the node id, for warnings; -1 if unknown
     */
    private record NodeRef(String nodeId, @Nullable String port, @Nullable String compass, int pos) {

        static NodeRef of(String nodeId) {
            return new NodeRef(nodeId, null, null, -1);
        }
    }

    /**
     * @param value the value with all backslash escapes removed (legacy; used for data and labels)
     * @param html  true if the value was written as an HTML-like string {@code <...>}
     * @param raw   the value as written, without surrounding quotes and with only {@code \"} unescaped, so that
     *              record-label escapes such as {@code \<} survive
     */
    private record Attr(String key, String value, boolean html, String raw) {}

    /** Port reference as written: {@code port[:compass]}. */
    private record PortRef(String port, @Nullable String compass) {

        /** Parse a {@code headport}/{@code tailport} value such as {@code f0:e}; null if empty. */
        static @Nullable PortRef parse(String value) {
            if (value.isEmpty()) return null;
            int i = value.indexOf(':');
            return i < 0 ? new PortRef(value, null) : new PortRef(value.substring(0, i), value.substring(i + 1));
        }

        @Override
        public String toString() {
            return compass == null ? port : port + DotPorts.COMPASS_SEP + compass;
        }
    }

    /** An endpoint whose port is resolved once the whole document is known. */
    private record PendingPort(ICjEndpointMutable endpoint, String nodeId, PortRef ref, int pos) {}

    /** What port resolution needs to know about a DOT node; DOT node ids are document-wide. */
    private static final class NodeInfo {

        /** CJ nodes for this DOT node (more than one if it appears in several subgraphs). */
        final List<ICjNodeMutable> cjNodes = new ArrayList<>();
        @Nullable String shape;
        @Nullable Attr label;
        /** The CJ node whose node statement set the label, which declares the ports; null if none did. */
        @Nullable ICjNodeMutable labelNode;
        /** True if a node statement set the shape; false if it is a default or unset. */
        boolean explicitShape;

        void applyDefaults(Collection<Attr> attrs) {
            for (Attr a : attrs) {
                if ("shape".equals(a.key)) shape = a.value;
                else if ("label".equals(a.key)) label = a;
            }
        }

        void applyNodeStatement(Collection<Attr> attrs, ICjNodeMutable node) {
            applyDefaults(attrs);
            for (Attr a : attrs) {
                if ("shape".equals(a.key)) explicitShape = true;
                else if ("label".equals(a.key)) labelNode = node;
            }
        }

        /** The one CJ node that gets the ports: the one with the label, else the first. */
        ICjNodeMutable portOwner() {
            return labelNode != null ? labelNode : cjNodes.get(0);
        }
    }

    /** {@code node [...]} / {@code edge [...]} defaults in scope; a subgraph starts with a copy of its parent's. */
    private static final class Defaults {

        final Map<String, Attr> node;
        final Map<String, Attr> edge;

        Defaults() {
            this(new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        private Defaults(Map<String, Attr> node, Map<String, Attr> edge) {
            this.node = node;
            this.edge = edge;
        }

        Defaults child() {
            return new Defaults(new LinkedHashMap<>(node), new LinkedHashMap<>(edge));
        }
    }

    private static final String TAILPORT = "tailport";
    private static final String HEADPORT = "headport";

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

        /**
         * Read an HTML-like string {@code <...>} exactly the way Graphviz's lexer does: count raw
         * {@code '<'} and {@code '>'} characters and stop at the {@code '>'} that brings the depth back
         * to zero. The scanner is deliberately blind to both tag structure and quoting — a {@code '>'} in
         * running text ({@code <<B>2 > 1</B>>}) or inside an attribute value ({@code <<B title=">">t</B>>})
         * closes the string early, and a lone {@code '<'} ({@code <<B title="<">u</B>>}) leaves it
         * unterminated. Graphviz reports all three as syntax errors, so we must too; being cleverer here
         * would mean accepting input no Graphviz tool can read and, worse, round-tripping it back out.
         * <p>
         * Whether the resulting markup is well-formed HTML is a separate question that Graphviz answers
         * later ("mismatched tag ... in label of node a"), not a DOT syntax error, and not checked here.
         */
        String readAngleString() {
            skipWs();
            if (s.charAt(pos) != '<') throw new IllegalStateException("Expected '<' at pos " + pos);
            int start = pos;
            StringBuilder out = new StringBuilder();
            int depth = 0;
            while (!eof()) {
                char c = s.charAt(pos++);
                out.append(c);
                if (c == '<') {
                    depth++;
                } else if (c == '>') {
                    depth--;
                    if (depth == 0) return out.toString();
                }
            }
            throw new IllegalStateException("Unterminated HTML-like string starting at pos " + start
                    + ": " + depth + " unmatched '<' remain. Graphviz counts raw angle brackets here, so a '<'"
                    + " in text or in an attribute value must be written as '&lt;'");
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
                    skipWs();
                    int valStart = pos;
                    String val = readIdOrString();
                    String token = s.substring(valStart, pos);
                    // only an unquoted <...> is HTML; a quoted "<f0> a|<f1> b" is a plain (record) string
                    boolean isHtml = token.startsWith("<");
                    list.add(new Attr(key, val, isHtml, token.startsWith("\"") ? rawQuoted(token) : val));
                } else {
                    // attributes without value -> store as key=true
                    list.add(new Attr(key, "true", false, "true"));
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

        /**
         * Read the edge operator, which must match the kind of the enclosing graph: {@code ->} in a
         * {@code digraph}, {@code --} in a {@code graph}. DOT's grammar ties the two together
         * ({@code edgeop : '->' | '--'} with the choice fixed by the graph kind), and Graphviz reports the
         * mismatch as "syntax error near '--'". Subgraphs inherit the kind from the top-level graph, which
         * is what {@code directed} carries down.
         */
        String readEdgeOp(boolean directed) {
            skipWs();
            String expected = directed ? "->" : "--";
            String wrong = directed ? "--" : "->";
            if (match(expected)) {
                pos += 2;
                return expected;
            }
            if (match(wrong)) {
                throw new IllegalStateException("Edge operator '" + wrong + "' at pos " + pos + " does not match the"
                        + " graph kind: a '" + (directed ? DIGRAPH : GRAPH) + "' uses '" + expected + "' edges");
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

        /** The content of a quoted string token with only {@code \"} unescaped, as DOT defines it. */
        private static String rawQuoted(String token) {
            String inner = token.length() >= 2 && token.endsWith("\"") ? token.substring(1, token.length() - 1) : token.substring(1);
            return inner.replace("\\\"", "\"");
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

    private final Map<String, NodeInfo> nodeInfos = new LinkedHashMap<>();
    private final List<PendingPort> pendingPorts = new ArrayList<>();

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
                    labelTarget.setLabel(l -> l.addEntry(le -> {
                        le.value(val);
                        // so the writer can write it back as <...>, not as a quoted string
                        if (a.html) le.dataMutable(d -> d.add(DotPorts.DATA_HTML, true));
                    }));
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
    private void addEdge(ICjGraphMutable g, NodeRef a, NodeRef b, boolean directed, List<Attr> attrsAccum, Defaults defaults) {
        // The tail is the left node, the head the right one. tailport/headport override a port on the node id.
        PortRef tail = portOf(a, TAILPORT, attrsAccum, defaults);
        PortRef head = portOf(b, HEADPORT, attrsAccum, defaults);
        List<Attr> dataAttrs = attrsAccum.stream().filter(x -> !TAILPORT.equals(x.key) && !HEADPORT.equals(x.key)).toList();
        g.addEdge(e -> {
            e.addEndpoint(ep -> {
                ep.node(a.nodeId);
                if (tail != null) pendingPorts.add(new PendingPort(ep, a.nodeId, tail, a.pos));
                ep.direction(directed ? CjDirection.IN : CjDirection.UNDIR);
            });
            e.addEndpoint(ep -> {
                ep.node(b.nodeId);
                if (head != null) pendingPorts.add(new PendingPort(ep, b.nodeId, head, b.pos));
                ep.direction(directed ? CjDirection.OUT : CjDirection.UNDIR);
            });
            applyAttrsToHasDataAndLabel(dataAttrs, e);
        });
    }

    /** The edge's {@code tailport}/{@code headport} (statement, then defaults), else the port on the node id. */
    private static @Nullable PortRef portOf(NodeRef ref, String attrKey, List<Attr> attrs, Defaults defaults) {
        Attr attr = null;
        for (Attr x : attrs) if (attrKey.equals(x.key)) attr = x;
        if (attr == null) attr = defaults.edge.get(attrKey);
        if (attr != null) return PortRef.parse(attr.value);
        return ref.port == null ? null : new PortRef(ref.port, ref.compass);
    }

    /** Get or create the CJ node {@code id} in graph {@code g}; a new DOT node takes the node defaults in scope. */
    private ICjNodeMutable ensureNode(ICjGraphMutable g, Map<String, ICjNodeMutable> nodesById, String id, Defaults defaults) {
        return nodesById.computeIfAbsent(id, k -> {
            ICjNodeMutable n = createNode(g, k);
            NodeInfo info = nodeInfos.get(k);
            if (info == null) {
                info = new NodeInfo();
                info.applyDefaults(defaults.node.values());
                nodeInfos.put(k, info);
            }
            info.cjNodes.add(n);
            return n;
        });
    }

    /**
     * Resolve all port references the way Graphviz does: a port the node declares, else a compass point, else
     * warn and ignore. Adds the declared ports and the used compass points to the CJ nodes.
     */
    private void resolvePorts() {
        Map<String, Map<String, ICjPortMutable>> portsByNode = new LinkedHashMap<>();
        Map<String, Set<String>> declaredByNode = new LinkedHashMap<>();
        nodeInfos.forEach((id, info) -> {
            Attr label = info.label;
            boolean html = label != null && label.html;
            Set<String> declared = DotPorts.declaredPorts(label == null ? null : label.raw, html, info.shape);
            declaredByNode.put(id, declared);
            ICjNodeMutable owner = info.portOwner();
            Map<String, ICjPortMutable> ports = new LinkedHashMap<>();
            for (String portId : declared) owner.addPort(port -> {
                port.id(portId);
                ports.put(portId, port);
            });
            portsByNode.put(id, ports);
            // the ports exist only because of a default shape=record: keep that shape with them
            if (!declared.isEmpty() && !html && !info.explicitShape) {
                String shape = info.shape;
                owner.dataMutable(d -> {
                    if (d.jsonValue() != null) d.remove("shape");
                    d.add("shape", shape);
                });
            }
        });
        for (PendingPort pp : pendingPorts) {
            NodeInfo info = nodeInfos.get(pp.nodeId);
            Set<String> declared = declaredByNode.getOrDefault(pp.nodeId, Set.of());
            String port = pp.ref.port;
            String compass = pp.ref.compass;
            String portId = null;
            String problem;
            if (declared.contains(port)) {
                if (compass == null) {
                    portId = port;
                    problem = null;
                } else if (DotPorts.isCompassPoint(compass)) {
                    portId = ensureCompassPort(portsByNode.get(pp.nodeId), info.portOwner(), port, compass);
                    problem = null;
                } else {
                    problem = "'" + compass + "' is no compass point (" + String.join(", ", DotPorts.COMPASS_POINTS) + ")";
                }
            } else if (compass == null && DotPorts.isCompassPoint(port)) {
                portId = ensureCompassPort(portsByNode.get(pp.nodeId), info.portOwner(), null, port);
                problem = null;
            } else {
                problem = "the node declares " + (declared.isEmpty() ? "no ports" : "only ports " + String.join(", ", declared))
                        + (DotPorts.isCompassPoint(port) ? "" : ", and '" + port + "' is no compass point");
            }
            if (portId != null) {
                pp.endpoint.port(portId);
            } else {
                onContentError(new ContentError(ContentError.ErrorLevel.Warn,
                        "Port '" + pp.ref + "' of node '" + pp.nodeId + "' unrecognized and ignored: " + problem,
                        pp.pos < 0 ? Location.UNAVAILABLE : mapPosToLocation(pp.pos)));
                pp.endpoint.dataMutable(d -> {
                    d.add(DotPorts.DATA_PORT, port);
                    if (compass != null) d.add(DotPorts.DATA_COMPASS, compass);
                });
            }
        }
    }

    /** Add compass port {@code compass} (nested in port {@code parent}, if given) to {@code owner}, once. */
    private static String ensureCompassPort(Map<String, ICjPortMutable> ports, ICjNodeMutable owner, @Nullable String parent,
            String compass) {
        String portId = DotPorts.compassPortId(parent, compass);
        if (ports.containsKey(portId)) return portId;
        Consumer<ICjPortMutable> init = port -> {
            port.id(portId);
            port.dataMutable(d -> d.add(DotPorts.DATA_COMPASS, compass));
            ports.put(portId, port);
        };
        if (parent == null) owner.addPort(init);
        else ports.get(parent).addPort(init);
        return portId;
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
                parseStatements(p, g, tl.directed, new Defaults());
                p.skipWs();
                p.expect(CURLY_BRACE_CLOSE);
            });
            resolvePorts();
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

    private void parseStatements(Parser p, ICjGraphMutable g, boolean directed, Defaults defaults) {
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
                    List<NodeRef> firstSeg = readNodeRefOrGroup(p, g, directed, nodesById, defaults);
                    parseEdgeChainTail(p, g, directed, nodesById, firstSeg, new ArrayList<>(), defaults);
                    continue;
                } else {
                    // Anonymous subgraph block
                    ICjGraphMutable sub = createSubgraph(g, null);
                    p.expect(CURLY_BRACE_OPEN);
                    parseStatements(p, sub, directed, defaults.child());
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
                    List<NodeRef> firstSeg = readNodeRefOrGroup(p, g, directed, nodesById, defaults);
                    parseEdgeChainTail(p, g, directed, nodesById, firstSeg, new ArrayList<>(), defaults);
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
                        ICjNodeMutable owner = ensureNode(g, nodesById, nc.nodeId, defaults);
                        sub = createNodeSubgraph(owner, nc.innerId.isEmpty() ? null : nc.innerId);
                    } else {
                        sub = createSubgraph(g, subId);
                    }
                    p.expect(CURLY_BRACE_OPEN);
                    parseStatements(p, sub, directed, defaults.child());
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
                Map<String, Attr> target = NODE.equals(p.readKeyword()) ? defaults.node : defaults.edge;
                p.skipWs();
                if (p.peek() == '[') {
                    // not copied into data yet; kept in scope to resolve ports (shape, label, headport, tailport)
                    for (Attr a : p.readAttrList(null)) target.put(a.key, a);
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
                parseEdgeChainTail(p, g, directed, nodesById, List.of(first), attrs, defaults);
            } else {
                // Node statement. Its attr_list was consumed above, so apply it here (issue #133).
                ICjNodeMutable node = ensureNode(g, nodesById, first.nodeId, defaults);
                applyAttrsToHasDataAndLabel(attrs, node);
                nodeInfos.get(first.nodeId).applyNodeStatement(attrs, node);
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
            Map<String, ICjNodeMutable> nodesById, List<NodeRef> firstSeg, List<Attr> attrs, Defaults defaults) {
        List<List<NodeRef>> segments = new ArrayList<>();
        segments.add(firstSeg);
        while (true) {
            p.skipWs();
            if (!p.lookaheadEdgeOp()) break;
            p.readEdgeOp(directed);
            segments.add(readNodeRefOrGroup(p, g, directed, nodesById, defaults));
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
                    ensureNode(g, nodesById, a.nodeId, defaults);
                    ensureNode(g, nodesById, b.nodeId, defaults);
                    addEdge(g, a, b, directed, attrs, defaults);
                }
            }
        }
        p.consumeOptionalSemicolon();
    }

    private NodeRef readNodeRef(Parser p) {
        p.skipWs();
        int pos = p.position();
        String id = p.readIdOrString();
        String port = null;
        String compass = null;
        p.skipWs();
        if (p.consumeIf(':')) {
            port = p.readIdOrString();
            p.skipWs();
            if (p.consumeIf(':')) {
                compass = p.readIdOrString();
            }
        }
        return new NodeRef(id, port, compass, pos);
    }

    /**
     * Read either a single nodeRef, a 'subgraph' endpoint with optional body, or a group { a, b, c }. If a 'subgraph'
     * keyword is used as an endpoint followed by an optional id and/or body, we create that subgraph on the current
     * graph and return a single NodeRef to a node literally named 'subgraph' to preserve DOT round-trip.
     */
    private List<NodeRef> readNodeRefOrGroup(Parser p, ICjGraphMutable g, boolean directed, Map<String, ICjNodeMutable> nodesById,
            Defaults defaults) {
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
                parseStatements(p, nestedRef[0], directed, defaults.child());
                p.skipWs();
                p.expect(CURLY_BRACE_CLOSE);
            }
            // Collect node ids inside the subgraph to expand the edge
            List<NodeRef> result = new ArrayList<>();
            nestedRef[0].nodes().forEach(n -> result.add(NodeRef.of(n.id())));
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
                        parseStatements(p, nestedRef[0], directed, defaults.child());
                        p.skipWs();
                        p.expect(CURLY_BRACE_CLOSE);
                    }
                    // add all nodes from subgraph to the group list
                    nestedRef[0].nodes().forEach(n -> list.add(NodeRef.of(n.id())));
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
