package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConnectedJsonReader implements GioReader {

    public static final GioFileFormat FORMAT = new GioFileFormat("connected-json", "Connected JSON Format", //
            ".con.json", ".con.json5", ".connected.json", ".connected.json5");
    private static final Logger log = LoggerFactory.getLogger(Json5Reader.class);
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(content);
            parse(rootNode, writer);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON5 content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON5 content", e);
        }
    }

    private void ensureNodeExists(String nodeId, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        if (!createdNodes.containsKey(nodeId)) {
            writer.startNode(GioNode.builder().id(nodeId).build());
            writer.endNode(null);
            createdNodes.put(nodeId, nodeId);
        }
    }

    private void parse(JsonNode rootNode, GioWriter writer) throws IOException {
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Map<String, String> createdNodes = new HashMap<>();

        // Process nodes
        JsonNode nodesNode = rootNode.get("nodes");
        if (nodesNode != null && nodesNode.isArray()) {
            for (JsonNode nodeJson : nodesNode) {
                processNode(nodeJson, writer, createdNodes);
            }
        }

        // Process edges
        JsonNode edgesNode = rootNode.get("edges");
        if (edgesNode != null && edgesNode.isArray()) {
            for (JsonNode edgeJson : edgesNode) {
                processEdge(edgeJson, writer, createdNodes);
            }
        }

        writer.endGraph(null);
        writer.endDocument();
    }

    private void processEdge(JsonNode edgeJson, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        // Handle simple source/target format
        JsonNode sourceNode = edgeJson.get("source");
        JsonNode targetNode = edgeJson.get("target");

        if (sourceNode != null && targetNode != null) {
            String sourceId = sourceNode.isTextual() ? sourceNode.asText() : String.valueOf(sourceNode.asInt());
            String targetId = targetNode.isTextual() ? targetNode.asText() : String.valueOf(targetNode.asInt());

            // Ensure nodes exist
            ensureNodeExists(sourceId, writer, createdNodes);
            ensureNodeExists(targetId, writer, createdNodes);

            // Create edge
            List<GioEndpoint> endpoints = new ArrayList<>();
            endpoints.add(GioEndpoint.builder().node(sourceId).build());
            endpoints.add(GioEndpoint.builder().node(targetId).build());

            GioEdge edge = GioEdge.builder().endpoints(endpoints).build();
            writer.startEdge(edge);
            writer.endEdge();
            return;
        }

        // Handle endpoints format
        JsonNode endpointsNode = edgeJson.get("endpoints");
        if (endpointsNode != null && endpointsNode.isArray()) {
            List<GioEndpoint> endpoints = new ArrayList<>();

            for (JsonNode endpointJson : endpointsNode) {
                JsonNode nodeIdNode = endpointJson.get("node");
                JsonNode portNode = endpointJson.get("port");
                JsonNode directionNode = endpointJson.get("direction");

                if (nodeIdNode != null) {
                    String nodeId = nodeIdNode.isTextual() ? nodeIdNode.asText() : String.valueOf(nodeIdNode.asInt());
                    ensureNodeExists(nodeId, writer, createdNodes);

                    GioEndpoint.GioEndpointBuilder endpointBuilder = GioEndpoint.builder().node(nodeId);
                    if (portNode != null) {
                        endpointBuilder.port(portNode.asText());
                    }
                    endpoints.add(endpointBuilder.build());
                }
            }

            if (!endpoints.isEmpty()) {
                GioEdge edge = GioEdge.builder().endpoints(endpoints).build();
                writer.startEdge(edge);
                writer.endEdge();
            }
        }
    }

    private void processNode(JsonNode nodeJson, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        JsonNode idNode = nodeJson.get("id");
        if (idNode != null) {
            String nodeId = idNode.isTextual() ? idNode.asText() : String.valueOf(idNode.asInt());

            if (!createdNodes.containsKey(nodeId)) {
                writer.startNode(GioNode.builder().id(nodeId).build());
                writer.endNode(null);
                createdNodes.put(nodeId, nodeId);
            }
        }
    }

}
