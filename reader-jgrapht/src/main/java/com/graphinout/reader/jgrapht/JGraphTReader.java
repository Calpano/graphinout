package com.graphinout.reader.jgrapht;

import com.graphinout.base.cj.element.ICjChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.xml.XML;
import com.graphinout.reader.jgrapht.dot.DotReader;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.EventDrivenImporter;
import org.jgrapht.nio.ImportException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used by {@link DotReader} and {@link Graph6Reader}
 *
 * @param <N> node type is often {@link Integer} or {@link String}
 */
public class JGraphTReader<N> {

    enum Mode {
        /** round 1) graph first */
        Graph,
        /** round 2 ) graph, nodes */
        Nodes,
        /** round 3) edges */
        Edges
    }

    enum Kind {Node, Edge, Graph}

    /** for a node or edge */
    static class CurrentJgtEntity {

        final ICjChunkMutable chunk;
        final Object jgtEntity;

        CurrentJgtEntity(Object jgtEntity, ICjChunkMutable chunk) {
            assert chunk != null;
            this.jgtEntity = jgtEntity;
            this.chunk = chunk;
        }

    }

    public static final String GRAPH_ID = "ID";
    final SingleInputSource singleInputSource;
    private final Function<N, String> nodeToNodeIdFun;
    private final Supplier<EventDrivenImporter<N, Pair<N, N>>> importerSupplier;
    private final ICjStream cjStream;
    // FIXME use NodeMap to ensure we create each node only once
    private final Map<N, ICjNodeChunkMutable> nodeMap = new LinkedHashMap<>();
    EventDrivenImporter<N, Pair<N, N>> importer;
    /**
     * The current JGT entity (node: {@code <N>}, edge: {@link CurrentJgtEntity}, graph: {@code this}). We need to bring
     * together i.e. edge nodes and edge attributes, but the order of these two calls is undefined in JGT.
     */
    private CurrentJgtEntity currentJgtEntity;
    private Mode mode;
    private ICjGraphChunkMutable graphChunk;

    /**
     * @param inputSource     IO
     * @param importer        JGraphT
     * @param cjStream        output
     * @param nodeToNodeIdFun turns node type 'N' into string ids
     */
    public JGraphTReader(InputSource inputSource, Supplier<EventDrivenImporter<N, Pair<N, N>>> importer, ICjStream cjStream, Function<N, String> nodeToNodeIdFun) {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        this.singleInputSource = (SingleInputSource) inputSource;
        this.importerSupplier = importer;
        this.cjStream = cjStream;
        this.nodeToNodeIdFun = nodeToNodeIdFun;
        this.graphChunk = cjStream.createGraphChunk();
    }

    public static IJsonValue toJsonValue(IJsonFactory jsonFactory, String value, AttributeType jgtType) {
        return switch (jgtType) {
            case NULL -> jsonFactory.createNull();
            case BOOLEAN -> jsonFactory.createBoolean(Boolean.parseBoolean(value));
            case STRING, UNKNOWN, IDENTIFIER -> jsonFactory.createString(value);
            case INT -> jsonFactory.createInteger(Integer.parseInt(value));
            case LONG -> jsonFactory.createLong(Long.parseLong(value));
            case FLOAT -> jsonFactory.createFloat(Float.parseFloat(value));
            case DOUBLE -> jsonFactory.createDouble(Double.parseDouble(value));
            case HTML -> jsonFactory.createXmlString(value, XML.XmlSpace.default_);
        };
    }

    public void onDocumentEnd() {
        // left-overs
        maybeEmitEvent_startEntity();
    }

    /**
     * main entry point
     *
     * @throws IOException
     */
    public void read() throws IOException {
        try {
            cjStream.documentStart(cjStream.createDocumentChunk());

            // round 1) graph
            this.currentJgtEntity = new CurrentJgtEntity(this, graphChunk);
            readInMode(singleInputSource, Mode.Graph);

            // round 2) nodes
            readInMode(singleInputSource, Mode.Nodes);

            // round 3) edges
            readInMode(singleInputSource, Mode.Edges);

            // slight asymmetry: graph ends here, starts in onJgtGraph
            cjStream.graphEnd();
            cjStream.documentEnd();
        } catch (ImportException | IOException e) {
            if (e.getCause() instanceof IOException ioException) throw ioException;
            throw cjStream.sendContentError_Error("JGraphT error: " + e.getMessage(), e);
        }
    }

    private void addAttributeToChunk(String jgtAttName, Attribute jgtAtt, ICjChunkMutable cjChunk) {
        IJsonValue jsonValue = toJsonValue(cjStream.jsonFactory(), jgtAtt.getValue(), jgtAtt.getType());
        cjChunk.dataMutable(data -> {
            // due to our setup, we get duplicate data here
            if (!data.hasProperty(jgtAttName)) {
                data.addProperty(jgtAttName, jsonValue);
            }
        });
    }

    private void emitJgtEdge(Pair<N, N> jgtEdge, ICjEdgeChunkMutable edgeChunk) {
        if (mode == Mode.Edges) {
            String s = toNodeId(jgtEdge.getFirst());
            String t = toNodeId(jgtEdge.getSecond());
            edgeChunk.addEndpoint(ep -> ep.node(s));
            edgeChunk.addEndpoint(ep -> ep.node(t));
        }
    }

    private void emitJgtNode(N jgtNode, ICjNodeChunkMutable nodeChunk) {
        if (mode == Mode.Nodes) {
            String nodeId = toNodeId(jgtNode);
            nodeChunk.id(nodeId);
            cjStream.node(nodeChunk);
        }
    }

    /**
     * @param kind
     * @param jgtObject node: {@code <N>}, edge: {@link CurrentJgtEntity}, graph: {@code this}.
     */
    private void maybeEmitEvent_startEntity(Kind kind, Object jgtObject) {
        if (currentJgtEntity != null  //
                && !Objects.equals(currentJgtEntity, jgtObject)) {
            // emit event from current active object
            if (!(currentJgtEntity.chunk instanceof ICjNodeChunkMutable)) {
                maybeEmitEvent_startEntity();
            }
            currentJgtEntity = null;
        }
        if (currentJgtEntity == null) {
            // create one
            currentJgtEntity = switch (kind) {
                case Node -> {
                    N node = (N) jgtObject;
                    ICjNodeChunkMutable nodeChunk = nodeMap.computeIfAbsent(node, _ignored -> cjStream.createNodeChunk());
                    yield new CurrentJgtEntity(jgtObject, nodeChunk);
                }
                case Edge -> new CurrentJgtEntity(jgtObject, cjStream.createEdgeChunk());
                case Graph -> new CurrentJgtEntity(jgtObject, cjStream.createGraphChunk());
            };
        }
    }

    /**
     * If we have a top-level entity, that entity is now ready to be sent (node,edge) or started (graph).
     */
    @SuppressWarnings("unchecked")
    private void maybeEmitEvent_startEntity() {
        if (currentJgtEntity != null) {
            if (currentJgtEntity.jgtEntity.getClass().equals(Pair.class)) {
                emitJgtEdge((Pair<N, N>) currentJgtEntity.jgtEntity, (ICjEdgeChunkMutable) currentJgtEntity.chunk);
            } else if (currentJgtEntity.jgtEntity.getClass().equals(JGraphTReader.class)) {
                // graph
            } else {
                emitJgtNode((N) currentJgtEntity.jgtEntity, (ICjNodeChunkMutable) currentJgtEntity.chunk);
            }
            // avoid emitting twice
            currentJgtEntity = null;
        }
    }

    private void readInMode(SingleInputSource sis, Mode parseMode) throws IOException {
        try (InputStream in = sis.inputStream()) {
            mode = parseMode;
            registerJgtHandlers();
            importer.importInput(in);
            onDocumentEnd();
        }
    }

    /** register our handlers to JGT */
    private void registerJgtHandlers() {
        this.importer = importerSupplier.get();

        // == Graph
        importer.addGraphAttributeConsumer((attName, att) -> {
            // there are no subgraphs in jgraphT
            maybeEmitEvent_startEntity(Kind.Graph, this);
            assert currentJgtEntity.chunk instanceof ICjGraphChunkMutable;
            addAttributeToChunk(attName, att, currentJgtEntity.chunk);
        });

        // == Node
        importer.addVertexAttributeConsumer((node_attName, att) -> {
            if (att == null) return;
            N node = node_attName.getFirst();
            String attName = node_attName.getSecond();
            maybeEmitEvent_startEntity(Kind.Node, node);
            assert currentJgtEntity.chunk instanceof ICjNodeChunkMutable;
            addAttributeToChunk(attName, att, currentJgtEntity.chunk);
        });
        importer.addVertexConsumer(vertex -> {
            maybeEmitEvent_startEntity(Kind.Node, vertex);
            assert currentJgtEntity.chunk instanceof ICjNodeChunkMutable : currentJgtEntity.chunk.getClass();
        });
        importer.addVertexWithAttributesConsumer((vertex, atts) -> {
            maybeEmitEvent_startEntity(Kind.Node, vertex);
            assert currentJgtEntity.chunk instanceof ICjNodeChunkMutable;
            atts.forEach((attName, att) -> addAttributeToChunk(attName, att, currentJgtEntity.chunk));
        });

        // == Edge
        importer.addEdgeAttributeConsumer((edge_attName, att) -> {
            Pair<N, N> jgtEdge = edge_attName.getFirst();
            String attName = edge_attName.getSecond();
            maybeEmitEvent_startEntity(Kind.Edge, jgtEdge);
            assert currentJgtEntity != null && currentJgtEntity.getClass().equals(CurrentJgtEntity.class);
            addAttributeToChunk(attName, att, currentJgtEntity.chunk);
        });
        importer.addEdgeConsumer(jgtEdge -> {
            maybeEmitEvent_startEntity(Kind.Edge, jgtEdge);
            assert currentJgtEntity.chunk instanceof ICjEdgeChunkMutable : currentJgtEntity.chunk.getClass();
        });
        importer.addEdgeWithAttributesConsumer((edge, atts) -> {
            maybeEmitEvent_startEntity(Kind.Edge, edge);
            assert currentJgtEntity.chunk instanceof ICjEdgeChunkMutable;
            atts.forEach((attName, att) -> addAttributeToChunk(attName, att, currentJgtEntity.chunk));
        });

        importer.addImportEventConsumer(importEvent -> {
            // start and end of import
        });
    }

    /** post-process all graph-level and emit */
    private void startFromJgtGraph(ICjGraphChunkMutable graphChunk) {
        if (mode == Mode.Graph) {
            // move ID from data to 'native' id
            graphChunk.dataMutable(data -> {
                if (data.hasProperty(GRAPH_ID)) {
                    String id = data.jsonValue_().asObject().get_(GRAPH_ID).asString();
                    data.removeProperty(GRAPH_ID);
                    graphChunk.id(id);
                }
            });
            cjStream.graphStart(graphChunk);
        }
    }

    private String toNodeId(N vertex) {
        return nodeToNodeIdFun.apply(vertex);
    }

}
