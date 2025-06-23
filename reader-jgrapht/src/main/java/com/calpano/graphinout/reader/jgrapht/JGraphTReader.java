package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.EventDrivenImporter;
import org.jgrapht.nio.ImportException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JGraphTReader<N> {

    enum Mode {CollectAttributeTypes, NodeParsing, GraphAndEdgeParsing}

    enum Kind {Node, Edge, Graph}

    abstract static class JgtEntity {
        public final Map<String, JgtAttribute> attributes = new HashMap<>();

        public void addAttribute(String name, AttributeType type, String value) {
            attributes.computeIfAbsent(name, _k -> new JgtAttribute(name, type, value));
        }

        public void addAttributes(Map<String, Attribute> atts) {
            atts.forEach((k, v) -> attributes.computeIfAbsent(k, _k -> new JgtAttribute(k, v.getType(), v.getValue())));
        }

    }

    public static class Node<N> extends JgtEntity {

        public final N vertex;

        public Node(N v) {
            this.vertex = v;
        }

        public void updateWithAttributesFrom(Node<N> other) {
            super.attributes.putAll(other.attributes);
        }

    }

    public static class Edge<N> extends JgtEntity {

        public final Pair<N, N> jgtEdge;

        public Edge(Pair<N, N> jgtEdge) {
            this.jgtEdge = jgtEdge;
        }

    }

    public static class Graph extends JgtEntity {
    }

    public static final String GRAPH_ID = "ID";

    final SingleInputSource inputSource;
    final GioWriter writer;
    final Consumer<ContentError> errorHandler;
    private final AtomicReference<IOException> exception = new AtomicReference<>();
    private final Map<N, String> nodeIds = new HashMap<>();
    private final Function<N, String> nodeToNodeIdFun;
    private final Map<String, GioKey> nodeAtts = new HashMap<>();
    private final Map<String, GioKey> edgeAtts = new HashMap<>();
    private final Map<String, GioKey> graphAtts = new HashMap<>();
    private final Supplier<EventDrivenImporter<N, Pair<N, N>>> importerSupplier;
    EventDrivenImporter<N, Pair<N, N>> importer;
    Node<N> activeNode = null;
    Object activeObject = null;
    Stack<Graph> activeGraphs = new Stack<>();
    private Edge<N> activeEdge;
    private Mode mode;
    private Map<String, Node<N>> nodeMap = new HashMap<>();
    private LinkedHashMap<String, Node> nodes = new LinkedHashMap<>();
    private @Nullable String outermostGraphId = null;

    public JGraphTReader(InputSource inputSource, Supplier<EventDrivenImporter<N, Pair<N, N>>> importer, GioWriter writer, Consumer<ContentError> errorHandler, Function<N, String> nodeToNodeIdFun) {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        this.inputSource = (SingleInputSource) inputSource;
        this.importerSupplier = importer;
        this.writer = writer;
        this.errorHandler = errorHandler;
        this.nodeToNodeIdFun = nodeToNodeIdFun;
    }

    private static GioDataType toGioType(AttributeType attributeType) {
        return switch (attributeType) {
            case BOOLEAN -> GioDataType.typeBoolean;
            case INT -> GioDataType.typeInt;
            case LONG -> GioDataType.typeLong;
            case FLOAT -> GioDataType.typeFloat;
            case DOUBLE -> GioDataType.typeDouble;
            case STRING,
                    // ??? weird mappings
                    HTML, UNKNOWN, IDENTIFIER, NULL -> GioDataType.typeString;
        };
    }

    public void onEnd() {
        maybeEmitEvent();
    }

    public void read() throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;

        // here we read the inputStream of sis ...
        try {
            parseInMode(sis, Mode.CollectAttributeTypes);
            parseInMode(sis, Mode.NodeParsing);

            // and write the graph to our GioWriter
            writer.startDocument(GioDocument.builder().build());
            // write keys
            for (GioKey key : graphAtts.values()) {
                writer.key(key);
            }
            for (GioKey key : nodeAtts.values()) {
                writer.key(key);
            }
            for (GioKey key : edgeAtts.values()) {
                writer.key(key);
            }

            // outermost graph / root graph
            GioGraph.GioGraphBuilder<?, ?> builder = GioGraph.builder();
            if (outermostGraphId != null) {
                builder.id(outermostGraphId);
            }
            writer.startGraph(builder.build());
            // write nodes from nodeMap
            for (Map.Entry<String, Node<N>> entry : nodeMap.entrySet()) {
                String nodeId = entry.getKey();
                Node<N> node = entry.getValue();
                try {
                    writer.startNode(GioNode.builder().id(nodeId).build());
                    for (JgtAttribute nodeAtt : node.attributes.values()) {
                        String key = nodeAtts.get(nodeAtt.attributeName).getId();
                        writer.data(GioData.builder().key(key).value(nodeAtt.attributeValue).build());
                    }
                    writer.endNode(null);
                } catch (IOException ex) {
                    if (ex.getCause() instanceof IOException ioex && (exception.get() == null)) {
                        exception.set(ioex);
                    }
                    if (errorHandler != null)
                        errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + ex.getMessage(), null));
                }
            }

            parseInMode(sis, Mode.GraphAndEdgeParsing);

            writer.endGraph(null);
            writer.endDocument();
        } catch (ImportException | IOException e) {
            if (e.getCause() instanceof IOException ioex) throw ioex;
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + e.getMessage(), null));
            }
        }
        if (exception.get() != null) throw exception.get();
    }

    private void copyAttributeInfo(Map<String, JgtAttribute> attributes, GioKeyForType gioKeyForType, Map<String, GioKey> gioKeyMap) {
        attributes.forEach((attName, v) -> {
            String id = gioKeyForType.name().toLowerCase() + "-" + attName;
            gioKeyMap.computeIfAbsent(attName, k -> new GioKey(attName, id, gioKeyForType, null, toGioType(v.attributeType)));
        });
    }

    private void maybeEmitEvent() {
        if (activeEdge != null) {
            onEdge(activeEdge);
            activeEdge = null;
        } else if (activeNode != null) {
            onNode(activeNode);
            activeNode = null;
        } else if (!activeGraphs.isEmpty()) {
            onGraph(activeGraphs.pop(), activeGraphs.size());
        }
        activeObject = null;
    }

    private void maybeEmitEvent(Kind kind, Object jgtObject) {
        if (activeObject != null && !Objects.equals(activeObject, jgtObject)) {
            // emit event from current active object
            maybeEmitEvent();
        }
        if (activeObject == null) {
            // create one
            activeObject = jgtObject;
            switch (kind) {
                case Node -> activeNode = (Node<N>) new Node<>(jgtObject);
                case Edge -> activeEdge = new Edge<>((Pair<N, N>) jgtObject);
                case Graph -> activeGraphs.push(new Graph());
            }
        }
    }

    private void onEdge(Edge<N> edge) {
        switch (mode) {
            case CollectAttributeTypes -> {
                copyAttributeInfo(edge.attributes, GioKeyForType.Edge, edgeAtts);
            }
            case NodeParsing -> {
            }
            case GraphAndEdgeParsing -> {
                String s = toNodeId(edge.jgtEdge.getFirst());
                String t = toNodeId(edge.jgtEdge.getSecond());
                try {
                    writer.startEdge(GioEdge.builder().endpoints(Arrays.asList( //
                            GioEndpoint.builder().node(s).build(), //
                            GioEndpoint.builder().node(t).build() //
                    )).build());
                    for (JgtAttribute edgeAtt : edge.attributes.values()) {
                        String key = edgeAtts.get(edgeAtt.attributeName).getId();
                        writer.data(GioData.builder().key(key).value(edgeAtt.attributeValue).build());
                    }
                    writer.endEdge();
                } catch (IOException ex) {
                    if (ex.getCause() instanceof IOException ioex && (exception.get() == null)) {
                        exception.set(ioex);

                    }
                    if (errorHandler != null)
                        errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + ex.getMessage(), null));
                }

            }
        }

    }

    private void onGraph(Graph graph, int outerGraphCount) {
        switch (mode) {
            case CollectAttributeTypes -> {
                copyAttributeInfo(graph.attributes, GioKeyForType.Graph, graphAtts);
            }
            case NodeParsing -> {
                if (outerGraphCount == 0) {
                    // we are the root graph, outermost graph
                    Optional.ofNullable(graph.attributes.get(GRAPH_ID)).map(att -> att.attributeValue).ifPresent(id -> outermostGraphId = id);
                }
            }
            case GraphAndEdgeParsing -> {
                for (JgtAttribute graphAtt : graph.attributes.values()) {
                    String key = graphAtts.get(graphAtt.attributeName).getId();
                    if (key.equals(GRAPH_ID)) {
                        continue;
                    }
                    try {
                        writer.data(GioData.builder().key(key).value(graphAtt.attributeValue).build());
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof IOException ioex && (exception.get() == null)) {
                            exception.set(ioex);
                        }
                        if (errorHandler != null)
                            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + ex.getMessage(), null));
                    }
                }
            }
        }
    }

    private void onNode(Node<N> node) {
        switch (mode) {
            case CollectAttributeTypes -> {
                copyAttributeInfo(node.attributes, GioKeyForType.Node, nodeAtts);
            }
            case NodeParsing -> {
                String nodeId = toNodeId(node.vertex);
                nodeMap.put(nodeId, node);
            }
            case GraphAndEdgeParsing -> {
            }
        }
    }

    private void parseInMode(SingleInputSource sis, Mode parseMode) throws IOException {
        try (InputStream in = sis.inputStream()) {
            mode = parseMode;
            prepareParse();
            importer.importInput(in);
            onEnd();
        }
    }

    private void prepareParse() {
        this.importer = importerSupplier.get();
        nodeIds.clear();
        importer.addVertexAttributeConsumer((pair, att) -> {
            if (att == null) return;
            maybeEmitEvent(Kind.Node, pair.getFirst());
            String attName = pair.getSecond();
            String attValue = att.getValue();
            AttributeType attType = att.getType();
            activeNode.addAttribute(attName, attType, attValue);
        });
        importer.addVertexConsumer(vertex -> {
            maybeEmitEvent(Kind.Node, vertex);
        });
        importer.addVertexWithAttributesConsumer((vertex, atts) -> {
            maybeEmitEvent(Kind.Node, vertex);
            activeNode.addAttributes(atts);
        });
        importer.addEdgeAttributeConsumer((pair, atts) -> {
            maybeEmitEvent(Kind.Edge, pair.getFirst());
            String attName = pair.getSecond();
            String attValue = atts.getValue();
            AttributeType attType = atts.getType();
            activeEdge.addAttribute(attName, attType, attValue);
        });
        importer.addEdgeConsumer(edge -> {
            maybeEmitEvent(Kind.Edge, edge);
        });
        importer.addEdgeWithAttributesConsumer((edge, atts) -> {
            maybeEmitEvent(Kind.Edge, edge);
            activeEdge.addAttributes(atts);
        });
        importer.addGraphAttributeConsumer((attName, atts) -> {
            // TODO subgraphs in jgraphT?
            maybeEmitEvent(Kind.Graph, this);
            activeGraphs.peek().addAttribute(attName, atts.getType(), atts.getValue());
        });
        importer.addImportEventConsumer(importEvent->{
            // start and end of import
        });
    }

    private String toNodeId(N vertex) {
        return nodeIds.computeIfAbsent(vertex, nodeToNodeIdFun);
    }

}
