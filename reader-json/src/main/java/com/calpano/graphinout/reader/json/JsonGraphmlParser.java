package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioEndpointDirection;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.Location;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMapping;
import com.calpano.graphinout.reader.json.mapper.Link;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.calpano.graphinout.reader.json.JsonPathOnJackson.runJsonPathOnJackson;

public class JsonGraphmlParser {

    private final SingleInputSource inputSource;
    private final GioWriter writer;
    private final GraphmlJsonMapping jsonMapping;
    private final @Nullable Consumer<ContentError> errorHandler;
    private final Set<String> createdInlineNodes = new HashSet<>();
    private final Set<String> existingNodes = new HashSet<>();
    private final Set<String> linkedNodes = new HashSet<>();


    public JsonGraphmlParser(InputSource inputSource, GioWriter writer, GraphmlJsonMapping jsonMapping, @Nullable Consumer<ContentError> errorHandler) {
        this.inputSource = (SingleInputSource) inputSource;
        this.writer = writer;
        this.jsonMapping = jsonMapping;
        this.errorHandler = errorHandler;
    }

    public void read() throws IOException {
        writer.startDocument(GioDocument.builder().build());

        writer.key(GioKey.builder().forType(GioKeyForType.Node).id("label").attributeName("label").attributeType(GioDataType.typeString).build());
        writer.key(GioKey.builder().forType(GioKeyForType.Edge).id("linkLabel").attributeName("linkLabel").attributeType(GioDataType.typeString).build());

        writer.startGraph(GioGraph.builder().build());

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try (JsonParser jsonParser = mapper.getFactory().createParser(inputSource.inputStream())) {
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode jsonNodeRoot = mapper.readTree(jsonParser);
                JsonNode labelNode = runJsonPathOnJackson(jsonNodeRoot, jsonMapping.getLabel());
                String label = stringValue(labelNode, jsonMapping.getLabel(), jsonParser.currentLocation());
                String id = jsonNodeRoot.get(jsonMapping.getId()).asText();
                existingNodes.add(id);
                writer.startNode(GioNode.builder().id(id).build());
                writer.data(GioData.builder().key("label").value(label).build());
                writer.endNode(null);
                for (Link link : jsonMapping.getLinks()) {
                    if (link instanceof Link.LinkCreateNode createNode) {
                        writeEdgeCreate(writer, createNode, id, jsonNodeRoot, jsonParser.currentLocation());
                    } else if (link instanceof Link.LinkToExistingNode existingNode) {
                        writeEdgeExisting(writer, existingNode, id, jsonNodeRoot, jsonParser.currentLocation());
                    } else {
                        throw new IllegalStateException("Unknown link type " + link.getClass().getName());
                    }
                }
            }
        }

        // auto-create missing nodes
        Set<String> missingNodes = new HashSet<>(linkedNodes);
        missingNodes.removeAll(existingNodes);
        for (String missingNode : missingNodes) {
            writer.startNode(GioNode.builder().id(missingNode).build());
            writer.endNode(null);
        }

        writer.endGraph(null);
        writer.endDocument();
    }

    private List<ObjectNode> listNodeValues(JsonNode shouldBeArrayNode, String jsonPointer, JsonLocation jsonLocation) {
        if (!(shouldBeArrayNode instanceof ArrayNode)) {
            // mapping is wrong or JSON object in data file is wrong
            if (errorHandler != null)
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "Path ['" + jsonPointer + "'] does not resolve to a array node ", new Location(jsonLocation.getLineNr(), jsonLocation.getColumnNr())));
            return Collections.emptyList();
        } else {
            List<ObjectNode> result = new ArrayList<>();
            for (JsonNode node : shouldBeArrayNode) {
                result.add((ObjectNode) node);
            }
            return result;
        }
    }

    private List<String> listStringValues(JsonNode shouldBeArrayNode, String jsonPointer, JsonLocation jsonLocation) {
        if (!(shouldBeArrayNode instanceof ArrayNode)) {
            // mapping is wrong or JSON object in data file is wrong
            if (errorHandler != null)
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "Path ['" + jsonPointer + "'] does not resolve to a array node ", new Location(jsonLocation.getLineNr(), jsonLocation.getColumnNr())));
            return Collections.emptyList();
        } else {
            List<String> result = new ArrayList<>();
            for (JsonNode node : shouldBeArrayNode) {
                result.add(node.asText());
            }
            return result;
        }
    }

    private String stringValue(JsonNode shouldBeValueNode, String jsonPointer, JsonLocation jsonLocation) {
        if (!(shouldBeValueNode instanceof ValueNode)) {
            // mapping is wrong or JSON object in data file is wrong
            if (errorHandler != null)
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "Path ['" + jsonPointer + "'] does not resolve to a string value ", new Location(jsonLocation.getLineNr(), jsonLocation.getColumnNr())));
            return "";
        } else {
            return shouldBeValueNode.asText();
        }
    }

    private void writeEdgeCreate(GioWriter writer, Link.LinkCreateNode createNodeMapping, String sourceNodeId, JsonNode sourceNode, JsonLocation jsonLocation) throws IOException {
        JsonNode targetsArrayNode = runJsonPathOnJackson(sourceNode, createNodeMapping.getTarget());
        String linkLabelObject = createNodeMapping.getLinkLabel();

        List<ObjectNode> list = listNodeValues(targetsArrayNode, createNodeMapping.getTarget(), jsonLocation);
        if (!list.isEmpty()) {
            for (ObjectNode objectNode : list) {
                // node
                String id = stringValue(runJsonPathOnJackson(objectNode, createNodeMapping.id), createNodeMapping.id, jsonLocation);
                String label = stringValue(runJsonPathOnJackson(objectNode, createNodeMapping.label), createNodeMapping.label, jsonLocation);

                boolean isNew = this.createdInlineNodes.add(id);
                if (isNew) {
                    writer.startNode(GioNode.builder().id(id).build());
                    if (!label.isEmpty()) writer.data(GioData.builder().key("label").value(label).build());
                    writer.endNode(null);
                }

                // edge
                List<GioEndpoint> gioEndpoints = new ArrayList<>();
                gioEndpoints.add(GioEndpoint.builder().node(sourceNodeId).type(GioEndpointDirection.Out).build());
                gioEndpoints.add(GioEndpoint.builder().node(id).type(GioEndpointDirection.In).build());
                writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
                if (!linkLabelObject.isEmpty())
                    writer.data(GioData.builder().key("linkLabel").value(linkLabelObject).build());
                writer.endEdge();
            }
        }
    }

    private void writeEdgeExisting(GioWriter writer, Link.LinkToExistingNode existingNodeMapping, String sourceNodeId, JsonNode targetNode, JsonLocation jsonLocation) throws IOException {
        JsonNode targetObjectNode = runJsonPathOnJackson(targetNode, existingNodeMapping.getIdTarget());
        String linkLabelObject = existingNodeMapping.getLinkLabel();

        List<String> list = listStringValues(targetObjectNode, existingNodeMapping.getIdTarget(), jsonLocation);
        if (!list.isEmpty()) {
            List<GioEndpoint> gioEndpoints = new ArrayList<>();
            GioEndpoint gioEndpoint = GioEndpoint.builder().node(sourceNodeId).type(GioEndpointDirection.In).build();
            gioEndpoints.add(gioEndpoint);
            for (String targetId : list) {
                gioEndpoints.add(GioEndpoint.builder().node(targetId).type(GioEndpointDirection.Out).build());
                linkedNodes.add(targetId);
            }
            writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
            if (!linkLabelObject.isEmpty())
                writer.data(GioData.builder().key("linkLabel").value(linkLabelObject).build());
            writer.endEdge();
        }
    }

}
