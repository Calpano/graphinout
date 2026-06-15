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

import java.util.HashMap;
import java.util.Map;

/**
 * Parses GEXF (1.2draft) XML into a CJ stream. Reuses the same XML pipeline as the GraphML reader:
 * SAX -&gt; {@link com.graphinout.base.xml.sax.Sax2XmlWriter} -&gt; this {@link XmlWriter}.
 * <p>
 * Supported: graph (directed/undirected default), nodes (id, label), edges (id, source/target, type/direction, label,
 * weight) and attribute definitions plus {@code <attvalue>}s (mapped to CJ data, keyed by the attribute title). Graph
 * hierarchy (nested {@code <graph>}/{@code <nodes>}) is not supported and is reported as a content error.
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
    private int graphDepth = 0;

    /** node currently being built (between {@code <node>} and {@code </node>}) */
    private @Nullable ICjNodeChunkMutable currentNode;
    /** edge currently being built (between {@code <edge>} and {@code </edge>}) */
    private @Nullable ICjEdgeChunkMutable currentEdge;

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
            case GEXF, ATTRIBUTES, ATTVALUES, NODES, EDGES -> { /* containers: nothing to emit */ }
            case GRAPH -> graphStart(attributes);
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
            case NODE -> nodeEnd();
            case EDGE -> edgeEnd();
            default -> { /* containers: nothing to do */ }
        }
    }

    private void graphStart(Map<String, String> attributes) {
        graphDepth++;
        if (graphDepth > 1) {
            sendContentError_Warn("Nested GEXF <graph> (hierarchy) is not supported; ignoring", null);
            return;
        }
        String defaultEdgeType = attributes.get("defaultedgetype");
        // GEXF default is "undirected"; only "directed"/"mutual" make edges directed
        this.directedByDefault = isDirectedType(defaultEdgeType, false);
        ICjGraphChunkMutable graph = stream.createGraphChunk();
        stream.graphStart(graph);
    }

    private void graphEnd() {
        if (graphDepth == 1) {
            stream.graphEnd();
        }
        graphDepth--;
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
        String id = attributes.get("id");
        if (id == null) {
            sendContentError_Error("GEXF <node> without 'id'", locator());
            currentNode = null;
            return;
        }
        ICjNodeChunkMutable node = stream.createNodeChunk();
        node.id(id);
        String label = attributes.get("label");
        if (label != null) {
            node.addLabelWithoutLanguage(label);
        }
        currentNode = node;
    }

    private void nodeEnd() {
        if (currentNode != null) {
            stream.node(currentNode);
            currentNode = null;
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
        ICjEdgeChunkMutable edge = stream.createEdgeChunk();
        String id = attributes.get("id");
        if (id != null) {
            edge.id(id);
        }
        boolean directed = isDirectedType(attributes.get("type"), directedByDefault);
        edge.addEndpoint(ep -> ep.node(source).direction(directed ? CjDirection.IN : CjDirection.UNDIR));
        edge.addEndpoint(ep -> ep.node(target).direction(directed ? CjDirection.OUT : CjDirection.UNDIR));
        String label = attributes.get("label");
        if (label != null) {
            edge.addLabelWithoutLanguage(label);
        }
        String weight = attributes.get("weight");
        if (weight != null) {
            edge.addProperty("weight", weight);
        }
        currentEdge = edge;
    }

    private void edgeEnd() {
        if (currentEdge != null) {
            stream.edge(currentEdge);
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
        if (currentNode != null) {
            currentNode.addProperty(key, value);
        } else if (currentEdge != null) {
            currentEdge.addProperty(key, value);
        } else {
            sendContentError_Warn("GEXF <attvalue> outside of a node or edge; ignoring", null);
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
