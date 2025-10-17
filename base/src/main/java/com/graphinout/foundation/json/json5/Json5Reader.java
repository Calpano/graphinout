package com.graphinout.foundation.json.json5;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioEndpoint;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reader for JSON5 format (JSON with comments and other extensions). Handles the Connected JSON format with JSON5
 * syntax.
 */
public class Json5Reader implements GioReader {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    public static final String FORMAT_ID = "json5";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Connected JSON5 Format", ".json5");

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
            // Preprocess JSON5 content to make it compatible with standard JSON
            String preprocessedContent = Json5Preprocessor.toJson(content);

            // Use Jackson to parse JSON content
            try (JsonParser parser = JSON_FACTORY.createParser(preprocessedContent)) {
                processJson5ContentWithJackson(parser, writer);
            }

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

    private Object parseJsonValue(JsonParser parser, JsonToken token) throws IOException {
        switch (token) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                return parser.getValueAsString();
            case VALUE_NUMBER_INT:
                return parser.getIntValue();
            case VALUE_NUMBER_FLOAT:
                return parser.getDoubleValue();
            case VALUE_TRUE:
            case VALUE_FALSE:
                return parser.getBooleanValue();
            case START_ARRAY:
                List<Object> list = new ArrayList<>();
                while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                    list.add(parseJsonValue(parser, token));
                }
                return list;
            case START_OBJECT:
                Map<String, Object> struct = new HashMap<>();
                while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
                    if (token == JsonToken.FIELD_NAME) {
                        String fieldName = parser.getCurrentName();
                        token = parser.nextToken(); // Move to the value
                        Object value = parseJsonValue(parser, token);
                        struct.put(fieldName, value);
                    }
                }
                return struct;
            default:
                return null; // fallback
        }
    }

    private void processEdgeFromMap(Map<String, Object> edgeData, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        // Handle simple source/target format
        Object sourceObj = edgeData.get("source");
        Object targetObj = edgeData.get("target");

        if (sourceObj != null && targetObj != null) {
            String sourceId = sourceObj instanceof String ? (String) sourceObj : String.valueOf(sourceObj);
            String targetId = targetObj instanceof String ? (String) targetObj : String.valueOf(targetObj);

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
        Object endpointsObj = edgeData.get("endpoints");
        if (endpointsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> endpointsList = (List<Map<String, Object>>) endpointsObj;
            List<GioEndpoint> endpoints = new ArrayList<>();

            for (Map<String, Object> endpointData : endpointsList) {
                Object nodeIdObj = endpointData.get("node");
                Object portObj = endpointData.get("port");
                Object directionObj = endpointData.get("direction");

                if (nodeIdObj != null) {
                    String nodeId = nodeIdObj instanceof String ? (String) nodeIdObj : String.valueOf(nodeIdObj);
                    ensureNodeExists(nodeId, writer, createdNodes);

                    GioEndpoint.GioEndpointBuilder endpointBuilder = GioEndpoint.builder().node(nodeId);
                    if (portObj != null) {
                        endpointBuilder.port(String.valueOf(portObj));
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

    private void processJson5ContentWithJackson(JsonParser parser, GioWriter writer) throws IOException {
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Map<String, String> createdNodes = new HashMap<>();
        Map<String, Object> rootData = new HashMap<>();

        // Parse the entire JSON structure first
        JsonToken token = parser.nextToken();
        if (token != null) {
            Object parsedData = parseJsonValue(parser, token);
            if (parsedData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) parsedData;
                rootData.putAll(dataMap);
            }
        }

        // Process nodes
        Object nodesObj = rootData.get("nodes");
        if (nodesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodesList = (List<Map<String, Object>>) nodesObj;
            for (Map<String, Object> nodeData : nodesList) {
                processNodeFromMap(nodeData, writer, createdNodes);
            }
        }

        // Process edges
        Object edgesObj = rootData.get("edges");
        if (edgesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edgesList = (List<Map<String, Object>>) edgesObj;
            for (Map<String, Object> edgeData : edgesList) {
                processEdgeFromMap(edgeData, writer, createdNodes);
            }
        }

        writer.endGraph(null);
        writer.endDocument();
    }

    private void processNodeFromMap(Map<String, Object> nodeData, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        Object idObj = nodeData.get("id");
        if (idObj != null) {
            String nodeId = idObj instanceof String ? (String) idObj : String.valueOf(idObj);

            if (!createdNodes.containsKey(nodeId)) {
                writer.startNode(GioNode.builder().id(nodeId).build());
                writer.endNode(null);
                createdNodes.put(nodeId, nodeId);
            }
        }
    }

}
