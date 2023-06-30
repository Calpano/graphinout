package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.reader.json.mapper.GraphmlJsonMapping;
import com.calpano.graphinout.reader.json.mapper.Link;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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


        Link.LinkToExistingNode linkToExistingNode = null;
        Link.LinkCreateNode linkCreateNode = null;
        List<Link> links = jsonMapping.getLinks().stream().toList();
        for (Link link : links) {
            if (link instanceof Link.LinkToExistingNode g) {
                linkToExistingNode = g;

            } else if (link instanceof Link.LinkCreateNode g) {
                linkCreateNode = g;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        //  mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        try (JsonParser jsonParser = mapper.getFactory().createParser(inputSource.inputStream())) {

            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode jsonNodeRoot = mapper.readTree(jsonParser);

                // maybe there is a better way to do this
                String json = jsonNodeRoot.toString();
                // run jayway on json to run any JsonPath on it

                String label = runJsonPathOnJackson(jsonNodeRoot, jsonMapping.getLabel());
                String id = jsonNodeRoot.get(jsonMapping.getId()).asText();
                writer.startNode(GioNode.builder().id(id).build());
                writer.data(GioData.builder().key("label").value(label).build());
                writer.endNode(null);

                if (linkToExistingNode != null) {
                    JsonNode tmp = jsonNodeRoot;
                    for (String path : linkToExistingNode.idTarget.split("\\.")) {
                        tmp = tmp.get(path);
                        if (tmp == null) break;
                    }
                    if (tmp != null) {
                        // System.out.println(tmp.asText());
                    }
                }
//
//                if (linkCreateNode != null) {
//                    System.out.println(jsonNodeRoot.get(linkCreateNode.target).asText());
//                }
            }
        }
        writer.endGraph(null);
        writer.endDocument();
    }

    /**
     * @throws IOException when writing fails
     */
    public void read2() throws IOException {
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());
        Link.LinkToExistingNode linkToExistingNode = null;
        Link.LinkCreateNode linkCreateNode = null;
        List<Link> links = jsonMapping.getLinks().stream().toList();
        for (Link link : links) {
            if (link instanceof Link.LinkToExistingNode g) {
                linkToExistingNode = g;

            } else if (link instanceof Link.LinkCreateNode g) {
                linkCreateNode = g;
            }
        }
        try (JsonParser jParser = new JsonFactory().createParser(inputSource.inputStream());) {

            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                System.out.println(jParser.currentToken().id());

                if (jParser.currentToken() != JsonToken.START_ARRAY) {
                    System.out.println(jParser.currentToken());
                    jParser.nextToken();
                }
                while (jParser.currentToken() != JsonToken.END_OBJECT) {
                    readNode(jParser);
                    System.out.println(jParser.currentToken());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//                if (jParser.nextToken() == JsonToken.START_ARRAY) {
//                    while (jParser.nextToken() != JsonToken.END_ARRAY) {
//                        System.out.println(jParser.getText());
//                    }
//                }


//
//                String nodeId = jParser.getValueAsString(jsonMapper.getId());
//                Map<String,String> customAttribute = new HashMap<>();
//                customAttribute.put(jsonMapper.getLabel(), jParser.getValueAsString(jsonMapper.getLabel()));
//                writer.startNode(GioNode.builder().id(nodeId).customAttributes(customAttribute).build());
//                writer.endNode(null);
//                List<Link> links = jsonMapper.getLinks().stream().toList();
//                for(Link link:links){
//                    if(link instanceof  Link.LinkToExistingNode g){
//                        System.out.println(jParser.getValueAsString(g.idTarget));
//                List<GioEndpoint> gioEndpoints = new ArrayList<>();
//                if (endpoints.size() > 0) {
//                    gioEndpoints.add(GioEndpoint.builder().id(s.toString()).type(GioEndpointDirection.Out).build());
//                    endpoints.get(0).forEach(endpoint -> gioEndpoints.add(GioEndpoint.builder().node(endpoint.toString()).type(GioEndpointDirection.In).build()));
//                }
//                if (gioEndpoints.size() > 0) {
//                    writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
//                    writer.endEdge();
//                }
//                    }else if(link instanceof  Link.LinkCreateNode) {
//                    }


//        for (Integer s : nodes) {
//
//            writer.startNode(GioNode.builder().id(s.toString()).build());
//            writer.endNode(null);
//        }
//
//        for (Integer s : nodes) {

//            for (String format : pathBuilder.findLink(s)) {
//                List<List<Integer>> endpoints = ctx.read(String.format(format, s));
//                List<GioEndpoint> gioEndpoints = new ArrayList<>();
//                if (endpoints.size() > 0) {
//                    gioEndpoints.add(GioEndpoint.builder().id(s.toString()).type(GioEndpointDirection.Out).build());
//                    endpoints.get(0).forEach(endpoint -> gioEndpoints.add(GioEndpoint.builder().node(endpoint.toString()).type(GioEndpointDirection.In).build()));
//                }
//                if (gioEndpoints.size() > 0) {
//                    writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
//                    writer.endEdge();
//                }
//            }
//        }
        writer.endGraph(null);
        writer.endDocument();
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
