package com.graphinout.foundation.json.json5;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.graphinout.base.BaseOutput;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.GioReader;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reader for JSON5 format (JSON with comments and other extensions). Handles the Connected JSON format with JSON5
 * syntax.
 */
public class Json5Reader extends BaseOutput implements GioReader {

    public static final String FORMAT_ID = "json5";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Connected JSON5 Format", ".json5");
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
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
            throw sendContentError_Error("Failed to parse JSON5 content", e);
        }
    }

    private void ensureNodeExists(String nodeId, ICjStream writer, Map<String, String> createdNodes) {
        if (!createdNodes.containsKey(nodeId)) {
            // Create the node
            writer.node(writer.createNodeChunkWithId(nodeId));
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

    private void processEdgeFromMap(Map<String, Object> edgeData, ICjStream writer, Map<String, String> createdNodes) throws IOException {
        // Handle simple source/target format
        Object sourceObj = edgeData.get("source");
        Object targetObj = edgeData.get("target");

        if (sourceObj != null && targetObj != null) {
            String sourceId = sourceObj instanceof String ? (String) sourceObj : String.valueOf(sourceObj);
            String targetId = targetObj instanceof String ? (String) targetObj : String.valueOf(targetObj);

            // Ensure nodes exist
            ensureNodeExists(sourceId, writer, createdNodes);
            ensureNodeExists(targetId, writer, createdNodes);

            ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
            edgeChunk.addEndpoint(ep -> ep.node(sourceId));
            edgeChunk.addEndpoint(ep -> ep.node(targetId));
            writer.edge(edgeChunk);
            return;
        }

        // Handle endpoints format
        Object endpointsObj = edgeData.get("endpoints");
        if (endpointsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> endpointsList = (List<Map<String, Object>>) endpointsObj;

            ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
            for (Map<String, Object> endpointData : endpointsList) {
                Object nodeIdObj = endpointData.get("node");
                Object portObj = endpointData.get("port");
                Object directionObj = endpointData.get("direction");

                if (nodeIdObj != null) {
                    String nodeId = nodeIdObj instanceof String ? (String) nodeIdObj : String.valueOf(nodeIdObj);
                    ensureNodeExists(nodeId, writer, createdNodes);
                    edgeChunk.addEndpoint(ep -> {
                        ep.node(nodeId);
                        if (portObj != null) {
                            ep.port(String.valueOf(portObj));
                        }
                    });
                }
            }

            if (edgeChunk.endpoints().findAny().isPresent()) {
                writer.edge(edgeChunk);
            }
        }
    }


    private void processJson5ContentWithJackson(JsonParser parser, ICjStream writer) throws IOException {
        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

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

        writer.graphEnd();
        writer.documentEnd();
    }

    private void processNodeFromMap(Map<String, Object> nodeData, ICjStream writer, Map<String, String> createdNodes) throws IOException {
        Object idObj = nodeData.get("id");
        if (idObj != null) {
            String nodeId = idObj instanceof String ? (String) idObj : String.valueOf(idObj);

            if (!createdNodes.containsKey(nodeId)) {
                writer.node(writer.createNodeChunkWithId(nodeId));
                createdNodes.put(nodeId, nodeId);
            }
        }
    }

}
