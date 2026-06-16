package com.graphinout.reader.d2;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Line-based D2 parser with a brace-scope stack.
 * <p>
 * Supports:
 * <ul>
 *   <li>Shape declarations: {@code foo}, {@code foo: Label}</li>
 *   <li>Containers: {@code cloud: { server1; server2 }} (multi-line or single-line); reconstructed as a CJ
 *       node carrying a nested subgraph</li>
 *   <li>Dotted paths: {@code parent.child -> other}</li>
 *   <li>Connections: {@code a -> b}, {@code a -> b: label}, {@code a <-> b}, {@code a -- b} (undirected)</li>
 *   <li>Connection chains: {@code a -> b -> c}</li>
 *   <li>Attributes: {@code foo.style.color: red} promoted to node/edge data; other keywords captured</li>
 *   <li>{@code #} line comments and {@code """ ... """} block comments</li>
 * </ul>
 * Globs / matchers / {@code @import} are not supported (a warn is emitted for {@code @}).
 */
public class D2Lines2CjDocument {

    /** D2 attribute keywords; their last-path-segment marks an attribute (vs. a label declaration). */
    private static final Set<String> ATTR_KEYWORDS = Set.of(
            "shape", "label", "style", "icon", "near", "link", "tooltip",
            "width", "height", "direction", "top", "left",
            "source-arrowhead", "target-arrowhead",
            "grid-rows", "grid-columns", "class",
            "fill", "stroke", "stroke-width", "stroke-dash",
            "font-color", "font-size", "opacity", "border-radius",
            "bold", "italic", "underline", "filled", "multiple", "double-border", "3d", "animated",
            "shadow"
    );

    /** Connection operator: ->, <-, <->, -- (undirected). */
    private static final Pattern CONN_TOKEN = Pattern.compile(
            "(?<arrow><->|<-|->|--)"
    );

    /** A parsed shape, holding nested children and any data attributes, for tree reconstruction. */
    private static final class Shape {
        final String fullId;
        final @Nullable Shape parent;
        @Nullable String label;
        final Map<String, String> attributes = new LinkedHashMap<>();
        final Map<String, Shape> children = new LinkedHashMap<>();
        final List<Edge> childEdges = new ArrayList<>();
        boolean container; // explicitly opened as a container '{ ... }'

        Shape(String fullId, @Nullable Shape parent) {
            this.fullId = fullId;
            this.parent = parent;
        }
    }

    private static final class Edge {
        final String source;
        final String target;
        final boolean undirected;
        final @Nullable String label;
        final Map<String, String> attributes = new LinkedHashMap<>();

        Edge(String source, String target, boolean undirected, @Nullable String label) {
            this.source = source;
            this.target = target;
            this.undirected = undirected;
            this.label = label;
        }
    }

    private final ICjStream writer;
    private final @Nullable Consumer<ContentError> errorHandler;

    /** Roots of the shape forest (top-level shapes), keyed by full id. */
    private final Map<String, Shape> roots = new LinkedHashMap<>();
    /** Every shape by full dotted id (for fast lookup / edge endpoint reuse). */
    private final Map<String, Shape> allShapes = new LinkedHashMap<>();
    /** Top-level edges. */
    private final List<Edge> rootEdges = new ArrayList<>();

    private final Deque<String> containerStack = new ArrayDeque<>();
    private int lineNumber = 0;
    private boolean any = false;

    public D2Lines2CjDocument(ICjStream writer, @Nullable Consumer<ContentError> errorHandler) {
        this.writer = writer;
        this.errorHandler = errorHandler;
    }

    public void parse(String content) {
        String stripped = stripBlockComments(content);
        List<String> lines = splitLogicalLines(stripped);

        for (String raw : lines) {
            lineNumber++;
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;
            if (line.startsWith("@")) {
                warn("Imports are not supported: " + line);
                continue;
            }

            // Handle closing braces (may appear standalone)
            while (line.startsWith("}")) {
                if (!containerStack.isEmpty()) {
                    closeBrace();
                } else {
                    warn("Unmatched '}'");
                }
                line = line.substring(1).trim();
                if (line.isEmpty()) break;
            }
            if (line.isEmpty()) continue;

            // Handle single-line container open AND close: "foo: { ... }"
            int openBraceIdx = findUnquoted(line, '{');
            int closeBraceIdx = findUnquoted(line, '}');
            if (openBraceIdx >= 0 && closeBraceIdx > openBraceIdx) {
                String head = line.substring(0, openBraceIdx).trim();
                String inner = line.substring(openBraceIdx + 1, closeBraceIdx).trim();
                openBrace(head, true);
                if (!inner.isEmpty()) {
                    for (String stmt : splitOn(inner, ';')) {
                        String t = stmt.trim();
                        if (!t.isEmpty()) processStatement(t);
                    }
                }
                closeBrace();
                continue;
            }

            // Handle multi-line container open: "foo: {" or "foo {"  (head may be a connection => edge map)
            if (openBraceIdx >= 0) {
                String head = line.substring(0, openBraceIdx).trim();
                openBrace(head, false);
                String tail = line.substring(openBraceIdx + 1).trim();
                if (!tail.isEmpty()) processStatement(tail);
                continue;
            }

            // No braces — semicolons may still chain statements on one line
            for (String stmt : splitOn(line, ';')) {
                String t = stmt.trim();
                if (!t.isEmpty()) processStatement(t);
            }
        }

        if (!containerStack.isEmpty()) warn("Unclosed container(s): " + containerStack.size());
    }

    // ----- container open/close -----

    /** Sentinel pushed when the open brace scope is a shape container (not an edge-attribute map). */
    private static final Edge NO_EDGE_MAP = new Edge("", "", false, null);
    /** Per open-brace scope: the edge whose attribute-map this scope is, or {@link #NO_EDGE_MAP}. */
    private final Deque<Edge> edgeMapStack = new ArrayDeque<>();

    /**
     * Open a brace scope for {@code head}, pushing onto both stacks (always balanced with {@link #closeBrace()}).
     * Returns the edge if the head was a connection (edge-attribute map), else null.
     */
    private @Nullable Edge openBrace(String head, boolean singleLine) {
        if (head.endsWith(":")) head = head.substring(0, head.length() - 1).trim();
        if (head.isEmpty()) {
            warn("Anonymous container open '{' — ignored");
            containerStack.push("");
            edgeMapStack.push(NO_EDGE_MAP);
            return null;
        }
        // An edge-map: "a -> b: { ... }"
        if (containsConnectionOperator(head)) {
            Edge edge = parseConnectionHead(head);
            containerStack.push("");      // placeholder: edge-map body is not a shape scope
            edgeMapStack.push(edge);
            return edge;
        }
        // A shape container; the head may itself be "id: Label".
        String shapeHead = head;
        String label = null;
        int colon = findUnquotedColon(head);
        if (colon >= 0) {
            shapeHead = head.substring(0, colon).trim();
            label = stripQuotes(head.substring(colon + 1).trim());
        }
        // Normalize the (possibly quoted / dotted) head id to its unquoted dotted form.
        List<String> headSegs = new ArrayList<>();
        for (String seg : splitPath(shapeHead)) headSegs.add(stripQuotes(seg));
        String headId = joinPath(headSegs);
        String containerId = qualify(headId);
        Shape shape = ensureShape(containerId, label);
        shape.container = true;
        containerStack.push(headId);
        edgeMapStack.push(NO_EDGE_MAP);
        return null;
    }

    private void closeBrace() {
        if (!containerStack.isEmpty()) containerStack.pop();
        if (!edgeMapStack.isEmpty()) edgeMapStack.pop();
    }

    /** Process one logical statement (no braces, no semicolons). */
    void processStatement(String stmt) {
        // Inside an edge map, statements are attributes of that edge.
        Edge edgeMap = edgeMapStack.peek();
        if (edgeMap != null && edgeMap != NO_EDGE_MAP) {
            applyAttribute(edgeMap.attributes, stmt);
            return;
        }

        // Try connection first.
        if (containsConnectionOperator(stmt)) {
            parseConnection(stmt);
            return;
        }

        // Otherwise: declaration or attribute. Split on first unquoted ':'.
        int colon = findUnquotedColon(stmt);
        if (colon < 0) {
            // Bare id => declare node (split on dots to handle "a.b" hierarchies)
            List<String> segs = new ArrayList<>();
            for (String seg : splitPath(stmt.trim())) segs.add(stripQuotes(seg));
            if (segs.isEmpty()) return;
            String id = qualify(joinPath(segs));
            if (!id.isEmpty()) ensureShape(id, null);
            return;
        }

        String lhs = stmt.substring(0, colon).trim();
        String rhs = stripQuotes(stmt.substring(colon + 1).trim());

        // Walk LHS dotted path; strip quotes from each segment
        List<String> path = new ArrayList<>();
        for (String seg : splitPath(lhs)) path.add(stripQuotes(seg));
        if (path.isEmpty()) return;

        // Is the LAST segment an attribute keyword? Then everything before is the node path.
        String last = path.get(path.size() - 1);
        boolean lastIsAttr = ATTR_KEYWORDS.contains(last.toLowerCase());

        // A "style" sub-key: "node.style.<key>: value" => promote <key>=value to node data.
        // When the path has no node prefix (style is the first segment) the target is the current container shape.
        int styleIdx = indexOfIgnoreCase(path, "style");
        if (styleIdx >= 0 && styleIdx < path.size() - 1) {
            List<String> nodePath = path.subList(0, styleIdx);
            Shape shape;
            if (nodePath.isEmpty()) {
                shape = currentContainerShape();
            } else {
                String nodeId = qualify(joinPath(nodePath));
                shape = ensureShape(nodeId, null);
            }
            if (shape != null) {
                String attrKey = String.join(".", path.subList(styleIdx + 1, path.size()));
                shape.attributes.put(attrKey, rhs);
                return;
            }
        }

        if (lastIsAttr && path.size() >= 2) {
            List<String> nodePath = path.subList(0, path.size() - 1);
            String nodeId = qualify(joinPath(nodePath));
            if (last.equalsIgnoreCase("label")) {
                ensureShape(nodeId, rhs);
            } else {
                ensureShape(nodeId, null);
                // Other attribute keyword captured as data so it round-trips.
                Shape shape = allShapes.get(nodeId);
                if (shape != null) shape.attributes.put(last.toLowerCase(), rhs);
            }
        } else {
            // Plain "id: label"
            String nodeId = qualify(joinPath(path));
            ensureShape(nodeId, rhs);
        }
    }

    /** Apply "style.key: value" (or "key: value") attribute text to the given attribute map. */
    private void applyAttribute(Map<String, String> into, String stmt) {
        int colon = findUnquotedColon(stmt);
        if (colon < 0) return;
        String lhs = stmt.substring(0, colon).trim();
        String rhs = stripQuotes(stmt.substring(colon + 1).trim());
        List<String> path = new ArrayList<>();
        for (String seg : splitPath(lhs)) path.add(stripQuotes(seg));
        if (path.isEmpty()) return;
        int styleIdx = indexOfIgnoreCase(path, "style");
        String key;
        if (styleIdx >= 0 && styleIdx < path.size() - 1) {
            key = String.join(".", path.subList(styleIdx + 1, path.size()));
        } else {
            key = String.join(".", path);
        }
        into.put(key, rhs);
    }

    private static int indexOfIgnoreCase(List<String> path, String token) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).equalsIgnoreCase(token)) return i;
        }
        return -1;
    }

    /** Parse a connection that appears as an edge-map head (label allowed, no trailing brace). */
    private Edge parseConnectionHead(String head) {
        List<Edge> edges = new ArrayList<>();
        buildEdges(head, edges);
        if (edges.isEmpty()) {
            // Degenerate; create empty placeholder so attributes don't NPE.
            return new Edge("", "", false, null);
        }
        // attribute map applies to the (single) edge; register all edges
        Edge first = edges.get(0);
        for (int i = 1; i < edges.size(); i++) addEdge(edges.get(i));
        addEdge(first);
        return first;
    }

    private void parseConnection(String stmt) {
        List<Edge> edges = new ArrayList<>();
        buildEdges(stmt, edges);
        for (Edge e : edges) addEdge(e);
    }

    /** Tokenize a connection statement into one or more edges (without registering them). */
    private void buildEdges(String stmt, List<Edge> out) {
        // Separate trailing "...: label"
        int colon = findUnquotedColon(stmt);
        String label = null;
        String body = stmt;
        if (colon >= 0) {
            int lastConn = findLastConnectionOpStart(stmt);
            if (colon > lastConn) {
                label = stripQuotes(stmt.substring(colon + 1).trim());
                body = stmt.substring(0, colon).trim();
            }
        }

        Matcher m = CONN_TOKEN.matcher(body);
        List<String> nodeIds = new ArrayList<>();
        List<String> arrows = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            String left = body.substring(last, m.start()).trim();
            if (left.isEmpty()) {
                warn("Connection missing left endpoint: " + stmt);
                return;
            }
            nodeIds.add(qualify(stripQuotes(left)));
            arrows.add(m.group("arrow"));
            last = m.end();
        }
        String tail = body.substring(last).trim();
        if (tail.isEmpty()) {
            warn("Connection missing right endpoint: " + stmt);
            return;
        }
        nodeIds.add(qualify(stripQuotes(tail)));

        for (int i = 0; i < arrows.size(); i++) {
            String src = nodeIds.get(i);
            String tgt = nodeIds.get(i + 1);
            String arrow = arrows.get(i);
            String thisLabel = (i == 0) ? label : null;
            ensureShape(src, null);
            ensureShape(tgt, null);
            boolean undirected = arrow.equals("--");
            if (arrow.equals("<-")) {
                out.add(new Edge(tgt, src, false, thisLabel));
            } else if (arrow.equals("<->")) {
                out.add(new Edge(src, tgt, false, thisLabel));
                out.add(new Edge(tgt, src, false, null));
            } else {
                out.add(new Edge(src, tgt, undirected, thisLabel));
            }
        }
    }

    /** Register an edge in its owning container (or root). */
    private void addEdge(Edge e) {
        any = true;
        Shape container = currentContainerShape();
        if (container != null) {
            container.childEdges.add(e);
        } else {
            rootEdges.add(e);
        }
    }

    /** Emit the reconstructed shape forest and edges to the writer. */
    public void flush() {
        for (Shape s : roots.values()) emitShape(s);
        for (Edge e : rootEdges) emitEdge(e);
    }

    private void emitShape(Shape s) {
        boolean nested = !s.children.isEmpty() || !s.childEdges.isEmpty();
        if (!nested) {
            // simple node (single-shot)
            ICjNodeChunkMutable n = writer.createNodeChunk();
            n.id(s.fullId);
            if (s.label != null && !s.label.isEmpty()) n.addLabelWithoutLanguage(s.label);
            applyData(n, s.attributes);
            writer.node(n);
            return;
        }
        // container node carrying a nested subgraph
        ICjNodeChunkMutable n = writer.createNodeChunk();
        n.id(s.fullId);
        if (s.label != null && !s.label.isEmpty()) n.addLabelWithoutLanguage(s.label);
        applyData(n, s.attributes);
        writer.nodeStart(n);
        writer.graphStart(writer.createGraphChunk());
        for (Shape child : s.children.values()) emitShape(child);
        for (Edge e : s.childEdges) emitEdge(e);
        writer.graphEnd();
        writer.nodeEnd();
    }

    private void emitEdge(Edge e) {
        ICjEdgeChunkMutable edge = writer.createEdgeChunk();
        if (e.undirected) {
            edge.addEndpoint(ep -> ep.node(e.source).direction(CjDirection.UNDIR));
            edge.addEndpoint(ep -> ep.node(e.target).direction(CjDirection.UNDIR));
        } else {
            edge.addEndpoint(ep -> ep.node(e.source).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(e.target).direction(CjDirection.OUT));
        }
        if (e.label != null && !e.label.isEmpty()) edge.addLabelWithoutLanguage(e.label);
        applyData(edge, e.attributes);
        writer.edge(edge);
    }

    private void applyData(com.graphinout.base.cj.document.ICjHasDataMutable target, Map<String, String> attrs) {
        for (Map.Entry<String, String> a : attrs.entrySet()) {
            target.addProperty(a.getKey(), a.getValue());
        }
    }

    public boolean hasContent() {
        return any || !roots.isEmpty();
    }

    // -------- shape tree helpers --------

    /**
     * Ensure a shape exists for the full dotted id, attaching it under the longest existing container prefix.
     * <p>
     * Nesting is driven by explicit {@code {}} containers (and the dotted references that target their children),
     * not by blindly splitting every id on '.', so ids that legitimately contain dots (e.g. URIs) stay flat.
     */
    private Shape ensureShape(String fullId, @Nullable String label) {
        if (fullId.isEmpty()) return new Shape("", null);
        any = true;
        Shape existing = allShapes.get(fullId);
        if (existing != null) {
            if (label != null && !label.isEmpty()) existing.label = label;
            return existing;
        }
        Shape parent = longestContainerPrefix(fullId);
        Shape s = new Shape(fullId, parent);
        allShapes.put(fullId, s);
        if (parent == null) {
            roots.put(fullId, s);
        } else {
            parent.children.put(fullId, s);
        }
        if (label != null && !label.isEmpty()) s.label = label;
        return s;
    }

    /** Find the existing shape that is the longest dotted prefix of {@code fullId} (its container parent), or null. */
    private @Nullable Shape longestContainerPrefix(String fullId) {
        List<String> segs = splitPath(fullId);
        Shape best = null;
        StringBuilder acc = new StringBuilder();
        for (int i = 0; i < segs.size() - 1; i++) {
            if (i > 0) acc.append('.');
            acc.append(segs.get(i));
            Shape candidate = allShapes.get(acc.toString());
            if (candidate != null) best = candidate;
        }
        return best;
    }

    /** The shape for the currently open container, or null at top level. */
    private @Nullable Shape currentContainerShape() {
        if (containerStack.isEmpty()) return null;
        // Build qualified id of the current container from the stack (skip placeholder/empty entries).
        StringBuilder b = new StringBuilder();
        java.util.Iterator<String> it = containerStack.descendingIterator();
        boolean any = false;
        while (it.hasNext()) {
            String seg = it.next();
            if (seg.isEmpty()) continue;
            if (any) b.append('.');
            b.append(seg);
            any = true;
        }
        if (!any) return null;
        return allShapes.get(b.toString());
    }

    /** Prepend the current container path to an id. */
    private String qualify(String id) {
        if (id.isEmpty()) return id;
        if (containerStack.isEmpty()) return id;
        StringBuilder b = new StringBuilder();
        java.util.Iterator<String> it = containerStack.descendingIterator();
        while (it.hasNext()) {
            String seg = it.next();
            if (seg.isEmpty()) continue;
            b.append(seg).append('.');
        }
        b.append(id);
        return b.toString();
    }

    private void warn(String msg) {
        ContentError err = ContentError.of(ContentError.ErrorLevel.Warn, msg, Location.of(lineNumber, 1));
        Nullables.ifConsumerPresentAccept(errorHandler, err);
    }

    /** Remove """...""" blocks (greedy non-nested). */
    static String stripBlockComments(String s) {
        StringBuilder b = new StringBuilder(s.length());
        int pos = 0;
        while (pos < s.length()) {
            int start = s.indexOf("\"\"\"", pos);
            if (start < 0) {
                b.append(s, pos, s.length());
                break;
            }
            b.append(s, pos, start);
            int end = s.indexOf("\"\"\"", start + 3);
            if (end < 0) {
                b.append(s.substring(start));
                break;
            }
            pos = end + 3;
        }
        return b.toString();
    }

    /** Split content into logical lines. Currently just splits on '\n'. */
    static List<String> splitLogicalLines(String s) {
        return List.of(s.split("\\R", -1));
    }

    /** Split a string on a delimiter, ignoring delimiters inside double quotes. */
    static List<String> splitOn(String s, char delim) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (c == delim && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }

    /** Split a dotted path, but a literal-quoted segment (e.g. "foo.bar") is kept whole. */
    static List<String> splitPath(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
                continue;
            }
            if (c == '.' && !inQuotes) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (!cur.toString().isEmpty()) out.add(cur.toString().trim());
        return out;
    }

    static String joinPath(List<String> path) {
        return String.join(".", path);
    }

    /** Find an unquoted character. Returns -1 if not found. */
    static int findUnquoted(String s, char ch) {
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ch && !inQuotes) return i;
        }
        return -1;
    }

    /** Find the first unquoted ':' (used for "key: value" splits). */
    static int findUnquotedColon(String s) {
        return findUnquoted(s, ':');
    }

    static boolean containsConnectionOperator(String s) {
        return CONN_TOKEN.matcher(stripQuotedRegions(s)).find();
    }

    static int findLastConnectionOpStart(String s) {
        String masked = stripQuotedRegions(s);
        Matcher m = CONN_TOKEN.matcher(masked);
        int last = -1;
        while (m.find()) last = m.start();
        return last;
    }

    /** Replace quoted regions with same-length spaces, so positional matching skips them. */
    static String stripQuotedRegions(String s) {
        char[] arr = s.toCharArray();
        boolean inQuotes = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '"') {
                inQuotes = !inQuotes;
                arr[i] = ' ';
                continue;
            }
            if (inQuotes) arr[i] = ' ';
        }
        return new String(arr);
    }

    static String stripQuotes(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return unescape(s.substring(1, s.length() - 1));
        }
        return s;
    }

    /** Inverse of {@link D2Doc#quoteIfNeeded}'s escaping: turn {@code \"}/{@code \\} back into literal chars. */
    static String unescape(String s) {
        if (s.indexOf('\\') < 0) return s;
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == '"' || next == '\\') {
                    b.append(next);
                    i++;
                    continue;
                }
            }
            b.append(c);
        }
        return b.toString();
    }
}
