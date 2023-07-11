package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMapping;
import com.calpano.graphinout.reader.json.mapper.Link;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.calpano.graphinout.reader.json.JsonPathOnJackson.runJsonPathOnJackson;

public class JsonGraphmlParser {
    private final SingleInputSource inputSource;
    private final GioWriter writer;
    private final GraphmlJsonMapping jsonMapping;


    public JsonGraphmlParser(InputSource inputSource, GioWriter writer, GraphmlJsonMapping jsonMapping) throws IOException {
        this.inputSource = (SingleInputSource) inputSource;
        this.writer = writer;
        this.jsonMapping = jsonMapping;
    }

    public void read() throws IOException {
        writer.startDocument(GioDocument.builder().build());

        writer.key(GioKey.builder().forType(GioKeyForType.Node).id("label").attributeName("label").attributeType(GioDataType.typeString).build());

        writer.startGraph(GioGraph.builder().build());


        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        try (JsonParser jsonParser = mapper.getFactory().createParser(inputSource.inputStream())) {

            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode jsonNodeRoot = mapper.readTree(jsonParser);

                // maybe there is a better way to do this
                String json = jsonNodeRoot.toString();


                String label = (String) runJsonPathOnJackson(jsonNodeRoot, jsonMapping.getLabel());
                String id = jsonNodeRoot.get(jsonMapping.getId()).asText();
                writer.startNode(GioNode.builder().id(id).build());
                writer.data(GioData.builder().key("label").value(label).build());
                writer.endNode(null);
                jsonMapping.getLinks().stream().forEach(link -> {
                    try {
                        if (link instanceof Link.LinkCreateNode g) {

                            writeEdge(writer, g, jsonNodeRoot, mapper, id);

                        } else {
                            writeEdge(writer, (Link.LinkToExistingNode) link, jsonNodeRoot, mapper, id);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

//
//                if (linkCreateNode != null) {
//                    System.out.println(jsonNodeRoot.get(linkCreateNode.target).asText());
//                }
            }
        }
        writer.endGraph(null);
        writer.endDocument();
    }

    private void writeEdge(GioWriter writer, Link.LinkCreateNode createNode, JsonNode jsonNode, ObjectMapper mapper, String nodeId) throws IOException {
        Object targetObject = runJsonPathOnJackson(jsonNode, createNode.getTarget());
        String targetJsonString = null;
        List<?> targetJsonList = null;
        if (targetObject instanceof String s)
            targetJsonString = s;
        else if (targetObject instanceof List<?> l) {
            targetJsonList = l;
        } else
            return;
        try (JsonParser jsonParser = mapper.getFactory().createParser(targetJsonString)) {
            List<GioEndpoint> gioEndpoints = new ArrayList<>();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode jsonNodeTargets = mapper.readTree(jsonParser);
                String linkLabel = (String) runJsonPathOnJackson(jsonNodeTargets, createNode.getLinkLabel());

                String label = (String) runJsonPathOnJackson(jsonNodeTargets, createNode.getLabel());
                String id = jsonNodeTargets.get(createNode.getId()).asText();
                writer.startNode(GioNode.builder().id(id).build());
                writer.data(GioData.builder().key("label").value(label).build());
                writer.endNode(null);
                gioEndpoints.add(GioEndpoint.builder().node(id).build());
            }
            writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
            writer.endEdge();
        }


    }

    private void writeEdge(GioWriter writer, Link.LinkToExistingNode existingNode, JsonNode jsonNode, ObjectMapper mapper, String nodeId) throws IOException {
        Object targetObject = runJsonPathOnJackson(jsonNode, existingNode.getIdTarget());
        Object linkLabelObject = runJsonPathOnJackson(jsonNode, existingNode.getLinkLabel());


        if (targetObject instanceof List<?> l) {
            List<GioEndpoint> gioEndpoints = new ArrayList<>();
            GioEndpoint gioEndpoint = GioEndpoint.builder().node(nodeId).type(GioEndpointDirection.Out).build();

            if (linkLabelObject != null)
                gioEndpoint.customAttribute(existingNode.getLinkLabel(), linkLabelObject.toString());

            gioEndpoints.add(gioEndpoint);
            l.forEach(o -> gioEndpoints.add(GioEndpoint.builder().node(o.toString()).type(GioEndpointDirection.In).build()));
            writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
            writer.endEdge();
        } else {
            //TODO
        }


    }

    private void readNode(JsonParser jParser) throws IOException {
        String nodeId = "";

        Map<String, String> customAttribute = new HashMap<>();

        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            if (jsonMapping.getId().equals(jParser.getCurrentName())) {
                nodeId = jParser.getValueAsString();
            } else if (jsonMapping.getLabel().equals(jParser.getCurrentName())) {
                customAttribute.put(jParser.getCurrentName(), jParser.getValueAsString());
            }
        }
        writer.startNode(GioNode.builder().id(nodeId).customAttributes(customAttribute).build());
        writer.endNode(null);

//        else {
//            if (linkToExistingNode != null) {
//                String[] paths = linkToExistingNode.idTarget.split("\\.");
//                for (String path : paths)
//                    if (path.equals(jParser.getCurrentName())) {
//                        jParser.nextToken();
//                        System.out.println(linkToExistingNode.idTarget);
//                        System.out.println(jParser.getValueAsString());
//                    }
//            }
//            if (linkCreateNode != null) {
//                String[] paths = linkCreateNode.target.split("\\.");
//                for (String path : paths)
//                    if (path.equals(jParser.getCurrentName())) {
//                        jParser.nextToken();
//                        System.out.println(linkCreateNode.target);
//                        System.out.println(jParser.getValueAsString());
//                    }
//            }
//        }
    }
}
