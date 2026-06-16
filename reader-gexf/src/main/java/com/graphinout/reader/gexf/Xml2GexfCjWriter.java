package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.xml.BaseXmlHandler;
import com.graphinout.foundation.pure.xml.CharactersKind;
import com.graphinout.foundation.pure.xml.writer.XmlWriter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses GEXF (1.2draft / 1.3) XML into a CJ stream. Reuses the same XML pipeline as the GraphML reader:
 * SAX -&gt; {@link com.graphinout.base.xml.sax.Sax2XmlWriter} -&gt; this {@link XmlWriter}.
 * <p>
 * Supported: graph (directed/undirected default), nodes (id, label), edges (id, source/target, type/direction,
 * {@code kind} =&gt; edge type, label, weight), attribute definitions plus {@code <attvalue>}s (mapped to CJ data, keyed
 * by the attribute title), graph hierarchy (nested {@code <nodes>}/{@code <edges>} inside a {@code <node>} =&gt; a CJ
 * sub-graph on that node) and graph-level {@code <attvalues>} (mapped to graph data).
 */
public class Xml2GexfCjWriter extends BaseXmlHandler implements XmlWriter {

    private static final String GEXF = "gexf";
    private static final String GRAPH = "graph";
    private static final String NODES = "nodes";
    private static final String NODE = "node";
    private static final String EDGES = "edges";
    private static final String EDGE = "edge";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTE = "attribute";
    private static final String ATTVALUES = "attvalues";
    private static final String ATTVALUE = "attvalue";

    private final ICjStream stream;

    /** maps GEXF attribute id -&gt; human title, to resolve {@code <attvalue for="...">} references */
    private final Map<String, String> attributeTitles = new HashMap<>();

    private boolean directedByDefault = false;

    /**
     * A node being built between {@code <node>} and {@code </node>}. Its {@code nodeStart} is deferred (the chunk is
     * fired in one go) until its own {@code <attvalue>}s are collected, i.e. until a nested {@code <nodes>} opens or the
     * node closes. {@code started} tracks whether {@code stream.nodeStart} has fired yet.
     */
    private static final class OpenNode {
        final ICjNodeChunkMutable chunk;
        boolean started;
        OpenNode(ICjNodeChunkMutable chunk) { this.chunk = chunk; }
    }

    /** stack of currently open nodes (for graph hierarchy). The top node is the one being built. */
    private final Deque<OpenNode> nodeStack = new ArrayDeque<>();
    /** edge currently being built (between {@code <edge>} and {@code </edge>}); fired (start) only at {@code </edge>}. */
    private @Nullable ICjEdgeChunkMutable currentEdge;

    /**
     * The top-level graph chunk, created at {@code <graph>} but only fired via {@code graphStart} once its first real
     * child appears, so graph-level {@code <attvalue>}s (which precede the nodes) can still be attached to the chunk.
     */
    private @Nullable ICjGraphChunkMutable pendingGraph;
    private boolean topGraphStarted = false;
    /** &gt;0 while inside a nested graph (hierarchy) opened by a child {@code <nodes>} inside a {@code <node>}. */
    private int nestedGraphDepth = 0;

    public Xml2GexfCjWriter(ICjStream stream) {
        this.stream = stream;
    }

    @Override
    public void documentStart() {
        stream.documentStart(stream.createDocumentChunk());
    }

    @Override
    public void documentEnd() {
        stream.documentEnd();
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) {
        switch (localName) {
            case GEXF, ATTRIBUTES, ATTVALUES -> { /* containers: nothing to emit */ }
            case GRAPH -> graphStart(attributes);
            case NODES -> nodesStart();
            case EDGES -> { /* container: nothing to emit */ }
            case ATTRIBUTE -> attributeDefinition(attributes);
            case NODE -> nodeStart(attributes);
            case EDGE -> edgeStart(attributes);
            case ATTVALUE -> attvalue(attributes);
            default -> sendContentError_Warn("Ignoring unknown GEXF element <" + localName + ">", null);
        }
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) {
        switch (localName) {
            case GRAPH -> graphEnd();
            case NODES -> nodesEnd();
            case NODE -> nodeEnd();
            case EDGE -> edgeEnd();
            default -> { /* containers: nothing to do */ }
        }
    }

    private void graphStart(Map<String, String> attributes) {
        if (pendingGraph != null || topGraphStarted) {
            // a second top-level <graph> in one file is not expected; ignore extra graphs
            sendContentError_Warn("Multiple top-level GEXF <graph> elements; only the first is read", null);
            return;
        }
        String defaultEdgeType = attributes.get("defaultedgetype");
        // GEXF default is "undirected"; only "directed"/"mutual" make edges directed
        this.directedByDefault = isDirectedType(defaultEdgeType, false);
        // Defer graphStart until the first real child so graph-level <attvalue>s can be attached to the chunk.
        pendingGraph = stream.createGraphChunk();
    }

    /** Fire the buffered top-level graphStart (idempotent). */
    private void ensureTopGraphStarted() {
        if (pendingGraph != null && !topGraphStarted) {
            stream.graphStart(pendingGraph);
            topGraphStarted = true;
        }
    }

    private void graphEnd() {
        ensureTopGraphStarted();
        if (topGraphStarted) {
            stream.graphEnd();
            topGraphStarted = false;
        }
        pendingGraph = null;
    }

    /** A {@code <nodes>} inside an open {@code <node>} starts a nested sub-graph on that node (GEXF hierarchy). */
    private void nodesStart() {
        if (!nodeStack.isEmpty()) {
            // the parent node's own attvalues are now fully collected: fire it, then open the nested graph on it
            ensureNodeStarted();
            stream.graphStart(stream.createGraphChunk());
            nestedGraphDepth++;
        } else {
            // top-level <nodes>: the graph must be live before its nodes are streamed
            ensureTopGraphStarted();
        }
    }

    private void nodesEnd() {
        if (nestedGraphDepth > 0 && !nodeStack.isEmpty()) {
            stream.graphEnd();
            nestedGraphDepth--;
        }
    }

    private void attributeDefinition(Map<String, String> attributes) {
        String id = attributes.get("id");
        if (id == null) {
            sendContentError_Warn("GEXF <attribute> without 'id'; ignoring", null);
            return;
        }
        String title = attributes.getOrDefault("title", id);
        attributeTitles.put(id, title);
    }

    private void nodeStart(Map<String, String> attributes) {
        ensureTopGraphStarted();
        ICjNodeChunkMutable node = stream.createNodeChunk();
        String id = attributes.get("id");
        if (id == null) {
            sendContentError_Error("GEXF <node> without 'id'", locator());
        } else {
            node.id(id);
        }
        String label = attributes.get("label");
        if (label != null) {
            node.addLabelWithoutLanguage(label);
        }
        // Defer nodeStart: the chunk is fired atomically, so its <attvalue>s (which follow) must be collected first.
        nodeStack.push(new OpenNode(node));
    }

    /** Fire the deferred {@code nodeStart} for the top-of-stack node (idempotent). */
    private void ensureNodeStarted() {
        OpenNode top = nodeStack.peek();
        if (top != null && !top.started) {
            stream.nodeStart(top.chunk);
            top.started = true;
        }
    }

    private void nodeEnd() {
        if (!nodeStack.isEmpty()) {
            ensureNodeStarted();
            nodeStack.pop();
            stream.nodeEnd();
        }
    }

    private void edgeStart(Map<String, String> attributes) {
        String source = attributes.get("source");
        String target = attributes.get("target");
        if (source == null || target == null) {
            sendContentError_Error("GEXF <edge> missing source/target (source=" + source + ", target=" + target + ")", locator());
            currentEdge = null;
            return;
        }
        ensureTopGraphStarted();
        ICjEdgeChunkMutable edge = stream.createEdgeChunk();
        String id = attributes.get("id");
        if (id != null) {
            edge.id(id);
        }
        boolean directed = isDirectedType(attributes.get("type"), directedByDefault);
        edge.addEndpoint(ep -> ep.node(source).direction(directed ? CjDirection.IN : CjDirection.UNDIR));
        edge.addEndpoint(ep -> ep.node(target).direction(directed ? CjDirection.OUT : CjDirection.UNDIR));
        // GEXF 1.3 semantic edge type
        String kind = attributes.get("kind");
        if (kind != null && !kind.isBlank()) {
            edge.edgeType(kind);
        }
        String label = attributes.get("label");
        if (label != null) {
            edge.addLabelWithoutLanguage(label);
        }
        String weight = attributes.get("weight");
        if (weight != null) {
            edge.addProperty("weight", weight);
        }
        // Defer edgeStart to edgeEnd: the chunk is fired atomically, so any <attvalue> children must be collected first.
        currentEdge = edge;
    }

    private void edgeEnd() {
        if (currentEdge != null) {
            stream.edgeStart(currentEdge);
            stream.edgeEnd();
            currentEdge = null;
        }
    }

    private void attvalue(Map<String, String> attributes) {
        String forId = attributes.get("for");
        String value = attributes.get("value");
        if (forId == null || value == null) {
            sendContentError_Warn("GEXF <attvalue> missing 'for'/'value'; ignoring", null);
            return;
        }
        String key = attributeTitles.getOrDefault(forId, forId);
        if (currentEdge != null) {
            currentEdge.addProperty(key, value);
        } else if (!nodeStack.isEmpty()) {
            nodeStack.peek().chunk.addProperty(key, value);
        } else if (pendingGraph != null && !topGraphStarted) {
            // graph-level attvalue, attached to the chunk before graphStart fires
            pendingGraph.addProperty(key, value);
        } else {
            sendContentError_Warn("GEXF <attvalue> outside of a node, edge or graph; ignoring", null);
        }
    }

    /** @return true if the GEXF edge type means "directed" ("directed" or "mutual"); {@code fallback} when type is null */
    private static boolean isDirectedType(@Nullable String type, boolean fallback) {
        if (type == null) {
            return fallback;
        }
        return switch (type.toLowerCase()) {
            case "directed", "mutual" -> true;
            case "undirected" -> false;
            default -> fallback;
        };
    }

    // == GEXF carries all graph data in attributes; character content is not significant ==

    @Override
    public void characters(String characters, CharactersKind kind) { /* ignored */ }

    @Override
    public void charactersStart() { /* ignored */ }

    @Override
    public void charactersEnd() { /* ignored */ }

    @Override
    public void lineBreak() { /* ignored */ }

    @Override
    public void raw(String rawXml) { /* ignored */ }

}
