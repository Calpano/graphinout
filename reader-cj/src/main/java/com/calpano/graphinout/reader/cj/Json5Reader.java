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
import com.amazon.ion.IonReader;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonType;
import com.amazon.ion.system.IonSystemBuilder;
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

/**
 * Reader for JSON5 format (JSON with comments and other extensions).
 * Handles the Connected JSON format with JSON5 syntax.
 */
public class Json5Reader implements GioReader {

    private static final IonSystem ION_SYSTEM = IonSystemBuilder.standard().build();

    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("json5", "Connected JSON5 Format", ".json5");
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
            // Use Amazon Ion to parse JSON5 content directly
            // Ion can handle JSON format and has better support for JSON5 features
            IonReader reader = ION_SYSTEM.newReader(content);

            processJson5ContentWithIon(reader, writer);
            reader.close();

        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed to parse JSON5 content: " + e.getMessage(), null));
            }
            throw new IOException("Failed to parse JSON5 content", e);
        }
    }

    private void processJson5ContentWithIon(IonReader reader, GioWriter writer) throws IOException {
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Map<String, String> createdNodes = new HashMap<>();
        Map<String, Object> rootData = new HashMap<>();

        // Parse the entire JSON structure first
        parseIonStructure(reader, rootData);

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

    private void parseIonStructure(IonReader reader, Map<String, Object> result) throws IOException {
        IonType type = reader.next();
        if (type == IonType.STRUCT) {
            reader.stepIn();
            while ((type = reader.next()) != null) {
                String fieldName = reader.getFieldName();
                Object value = parseIonValue(reader);
                result.put(fieldName, value);
            }
            reader.stepOut();
        }
    }

    private Object parseIonValue(IonReader reader) throws IOException {
        IonType type = reader.getType();
        if (reader.isNullValue()) {
            return null;
        }

        switch (type) {
            case STRING:
                return reader.stringValue();
            case INT:
                return reader.intValue();
            case FLOAT:
                return reader.doubleValue();
            case BOOL:
                return reader.booleanValue();
            case LIST:
                List<Object> list = new ArrayList<>();
                reader.stepIn();
                while (reader.next() != null) {
                    list.add(parseIonValue(reader));
                }
                reader.stepOut();
                return list;
            case STRUCT:
                Map<String, Object> struct = new HashMap<>();
                reader.stepIn();
                while (reader.next() != null) {
                    String fieldName = reader.getFieldName();
                    Object value = parseIonValue(reader);
                    struct.put(fieldName, value);
                }
                reader.stepOut();
                return struct;
            default:
                return reader.stringValue(); // fallback
        }
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

    private void ensureNodeExists(String nodeId, GioWriter writer, Map<String, String> createdNodes) throws IOException {
        if (!createdNodes.containsKey(nodeId)) {
            writer.startNode(GioNode.builder().id(nodeId).build());
            writer.endNode(null);
            createdNodes.put(nodeId, nodeId);
        }
    }
}
