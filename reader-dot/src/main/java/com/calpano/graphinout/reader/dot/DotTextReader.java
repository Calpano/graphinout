package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class DotTextReader implements GioReader {

    private static final Logger log = getLogger(DotTextReader.class);
    public static final String QUOTES = "[\"']";
    public static final String EMPTY = "";
    public static final String NULL = "null";
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot", "DOT Text Format", ".dot", ".gv");
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;

        try {
            GraphParser parser = new GraphParser(sis.inputStream());
            Map<String, GraphNode> nodes = parser.getNodes();
            Map<String, GraphEdge> edges = parser.getEdges();

            writer.startDocument(GioDocument.builder().build());

            // send GioKey events for all used attribute types
            Set<String> usedNodeKeys = new HashSet<>();
            Set<String> usedEdgeKeys = new HashSet<>();
            Set<String> usedKeys = new HashSet<>();
            nodes.values().stream().flatMap(node -> node.getAttributes().keySet().stream()).forEach(usedNodeKeys::add);
            edges.values().stream().flatMap(node -> node.getAttributes().keySet().stream()).forEach(usedEdgeKeys::add);
            usedKeys.addAll(usedNodeKeys);
            usedKeys.addAll(usedEdgeKeys);
            for (String key : usedKeys) {
                GioKeyForType keyForType = GioKeyForType.All;
                if (usedNodeKeys.contains(key) && usedEdgeKeys.contains(key)) {
                    // ok
                } else if (usedNodeKeys.contains(key)) {
                    keyForType = GioKeyForType.Node;
                } else if (usedEdgeKeys.contains(key)) {
                    keyForType = GioKeyForType.Edge;
                }
                GioKey gioKey = GioKey.builder().id(key).forType(keyForType).build();
                writer.key(gioKey);
            }

            writer.startGraph(GioGraph.builder().build());

            log.info("--- nodes:");
            for (GraphNode node : nodes.values()) {
                Map<String, Object> attributes = node.getAttributes();
                String nodeId = node.getId().replaceAll(QUOTES, EMPTY);
                GioNode gioNode = GioNode.builder().id(nodeId).build();
                writer.startNode(gioNode);

                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    String key = attribute.getKey();
                    String value = String.valueOf(attribute.getValue());

                    GioData gioData = GioData.builder().key(key).value(value).build();
                    writer.data(gioData);
                }

                writer.endNode(null);
                log.info(node.getId() + " " + node.getAttributes());
            }

            log.info("--- edges:");
            for (GraphEdge dotEdge : edges.values()) {
                Map<String, Object> attributes = dotEdge.getAttributes();

                String edgeId = dotEdge.getId().replaceAll(QUOTES, EMPTY);
                String sourceNodeId = dotEdge.getNode1().getId().replaceAll(QUOTES, EMPTY);
                String targetNodeId = dotEdge.getNode2().getId().replaceAll(QUOTES, EMPTY);

                GioEndpointDirection direction = GioEndpointDirection.Undirected;
                if (edges.values().stream().anyMatch(edge -> edge.getNode2() == dotEdge.getNode1() && edge.getNode1() == dotEdge.getNode2())) {
                    direction = GioEndpointDirection.Out;
                }

                GioEndpoint sourceEndpoint = GioEndpoint.builder()
                        .node(sourceNodeId)
                        .type(direction)
                        .build();
                GioEndpoint targetEndpoint = GioEndpoint.builder()
                        .node(targetNodeId)
                        .type(direction.isDirected() ? GioEndpointDirection.In : GioEndpointDirection.Undirected)
                        .build();

                writer.startEdge(GioEdge.builder()
                        .id(edgeId)
                        .endpoint(sourceEndpoint)
                        .endpoint(targetEndpoint)
                        .build());

                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    String edgeKey = attribute.getKey();
                    String edgeValue = String.valueOf(attribute.getValue());
                    GioData gioData = GioData.builder().key(edgeKey).value(edgeValue).build();

                    if (edgeKey != null && !edgeKey.equals(NULL)) {
                        writer.data(gioData);
                    }
                }
                writer.endEdge();
                log.info(dotEdge.getNode1().getId() + "->" + dotEdge.getNode2().getId() + " " + dotEdge.getAttributes());
            }
            writer.endGraph(null);
            writer.endDocument();
        } catch (GraphParserException e) {
            ContentError.ErrorLevel errorLevel = ContentError.ErrorLevel.Error;
            String errorMessage = e.getMessage();

            if (errorHandler != null) {
                errorHandler.accept(new ContentError(errorLevel, errorMessage, null));
            }
        }
    }
}
