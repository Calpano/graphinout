package com.graphinout.reader.d2;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.ArrayDeque;
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
 *   <li>Containers: {@code cloud: { server1; server2 }} (multi-line or single-line)</li>
 *   <li>Dotted paths: {@code parent.child -> other}</li>
 *   <li>Connections: {@code a -> b}, {@code a -> b: label}, {@code a <-> b}, {@code a -- b}</li>
 *   <li>Connection chains: {@code a -> b -> c}</li>
 *   <li>Attributes: {@code foo.shape: cylinder}, {@code foo.style.fill: red} (recorded but not promoted)</li>
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

    /** Connection operator: ->, <-, <->, -- (undirected), --> (treated as ->), -> with optional double dashes. */
    private static final Pattern CONN_TOKEN = Pattern.compile(
            "(?<arrow><->|<-|->|--)"
    );

    private final ICjStream writer;
    private final @Nullable Consumer<ContentError> errorHandler;

    private final Map<String, ICjNodeChunkMutable> nodes = new LinkedHashMap<>();
    private final List<ICjEdgeChunk> edgeBuffer = new ArrayList<>();
    private final Deque<String> containerStack = new ArrayDeque<>();
    private int lineNumber = 0;

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
                if (!containerStack.isEmpty()) containerStack.pop();
                else warn("Unmatched '}'");
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
                if (head.endsWith(":")) head = head.substring(0, head.length() - 1).trim();
                if (!head.isEmpty()) {
                    String containerId = qualify(head);
                    ensureNode(containerId, null);
                    containerStack.push(head);
                }
                if (!inner.isEmpty()) {
                    for (String stmt : splitOn(inner, ';')) {
                        String t = stmt.trim();
                        if (!t.isEmpty()) processStatement(t);
                    }
                }
                if (!head.isEmpty()) containerStack.pop();
                // any trailing content after '}' ignored
                continue;
            }

            // Handle multi-line container open: "foo: {" or "foo {"
            if (openBraceIdx >= 0) {
                String head = line.substring(0, openBraceIdx).trim();
                if (head.endsWith(":")) head = head.substring(0, head.length() - 1).trim();
                if (!head.isEmpty()) {
                    String containerId = qualify(head);
                    ensureNode(containerId, null);
                    containerStack.push(head);
                } else {
                    warn("Anonymous container open '{' — ignored");
                }
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

    /** Process one logical statement (no braces, no semicolons). */
    void processStatement(String stmt) {
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
            if (!id.isEmpty()) ensureNode(id, null);
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

        if (lastIsAttr && path.size() >= 2) {
            List<String> nodePath = path.subList(0, path.size() - 1);
            String nodeId = qualify(joinPath(nodePath));
            if (last.equalsIgnoreCase("label")) {
                ensureNode(nodeId, rhs);
            } else {
                ensureNode(nodeId, null);
                // Attribute value silently captured (no data plumbing in v1); just ensure the node exists.
            }
        } else {
            // Plain "id: label"
            String nodeId = qualify(joinPath(path));
            ensureNode(nodeId, rhs);
        }
    }

    private void parseConnection(String stmt) {
        // Separate trailing "...: label"
        int colon = findUnquotedColon(stmt);
        String label = null;
        String body = stmt;
        if (colon >= 0) {
            // Take colon only if it's after the last connection token.
            int lastConn = findLastConnectionOpStart(stmt);
            if (colon > lastConn) {
                label = stripQuotes(stmt.substring(colon + 1).trim());
                body = stmt.substring(0, colon).trim();
            }
        }

        // Tokenize body into nodes and arrows
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

        // Emit one edge per arrow; only the FIRST arrow gets the label
        for (int i = 0; i < arrows.size(); i++) {
            String src = nodeIds.get(i);
            String tgt = nodeIds.get(i + 1);
            String arrow = arrows.get(i);
            String thisLabel = (i == 0) ? label : null;
            ensureNode(src, null);
            ensureNode(tgt, null);
            if (arrow.equals("<-")) {
                addEdge(tgt, src, thisLabel);
            } else {
                addEdge(src, tgt, thisLabel);
            }
            // bidirectional <-> creates an extra reverse edge
            if (arrow.equals("<->")) addEdge(tgt, src, null);
        }
    }

    /** Emit buffered nodes and edges to the writer. */
    public void flush() {
        for (ICjNodeChunkMutable n : nodes.values()) writer.node(n);
        for (ICjEdgeChunk e : edgeBuffer) writer.edge(e);
    }

    public boolean hasContent() {
        return !nodes.isEmpty() || !edgeBuffer.isEmpty();
    }

    // -------- helpers --------

    private boolean ensureNode(String id, @Nullable String label) {
        if (id.isEmpty()) return false;
        ICjNodeChunkMutable n = nodes.get(id);
        boolean isNew = false;
        if (n == null) {
            n = writer.createNodeChunk();
            n.id(id);
            nodes.put(id, n);
            isNew = true;
        }
        if (label != null && !label.isEmpty()) {
            n.addLabelWithoutLanguage(label);
        }
        return isNew;
    }

    private void addEdge(String source, String target, @Nullable String label) {
        ICjEdgeChunkMutable edge = writer.createEdgeChunk();
        edge.addEndpoint(ep -> ep.node(source).direction(CjDirection.IN));
        edge.addEndpoint(ep -> ep.node(target).direction(CjDirection.OUT));
        if (label != null && !label.isEmpty()) edge.addLabelWithoutLanguage(label);
        edgeBuffer.add(edge);
    }

    /** Prepend the current container path to an id; if id is already dotted-qualified, leave it alone if the first segment doesn't exist as a node yet. */
    private String qualify(String id) {
        if (id.isEmpty()) return id;
        if (containerStack.isEmpty()) return id;
        // Reverse stack into a prefix
        StringBuilder b = new StringBuilder();
        java.util.Iterator<String> it = containerStack.descendingIterator();
        while (it.hasNext()) {
            b.append(it.next()).append('.');
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
                // unterminated — append remainder
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
        // We look for unquoted "->", "<-", "<->", "--"
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
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
