package com.calpano.graphinout.base.gio.dom;

import com.calpano.graphinout.base.cj.helper.jackson.CjJacksonDocument;
import com.calpano.graphinout.base.cj.helper.jackson.CjJacksonEdge;
import com.calpano.graphinout.base.cj.helper.jackson.CjJacksonEndpoint;
import com.calpano.graphinout.base.cj.helper.jackson.CjJacksonGraph;
import com.calpano.graphinout.base.cj.helper.jackson.CjJacksonNode;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class CjJacksonParsingTest {

    @Test
    public void testParseConnectedJsonDocument() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // load resource "sample-1.cj.json"
        // Use ClassLoader to get the resource URL
        java.net.URL resourceUrl = getClass().getClassLoader().getResource("json/cj/extended/sample-1.cj.json");
        assertThat(resourceUrl).isNotNull();
        File jsonFile = new File(resourceUrl.getFile());
        CjJacksonDocument doc = objectMapper.readValue(jsonFile, CjJacksonDocument.class);

        // Test document-level nodes and edges
        assertThat(doc.getNodes()).isNotNull();
        assertThat(doc.getNodes()).hasSize(2);

        assertThat(doc.getEdges()).isNotNull();
        assertThat(doc.getEdges()).hasSize(2);

        // Test graphs
        assertThat(doc.getGraphs()).isNotNull();
        assertThat(doc.getGraphs()).hasSize(1);

        // Test first node
        CjJacksonNode firstNode = doc.getNodes().getFirst();
        assertThat(firstNode.getId()).isEqualTo("node1");
        assertThat(firstNode.getLabel()).isEqualTo("First Node");

        // Test second node with ports
        CjJacksonNode secondNode = doc.getNodes().get(1);
        assertThat(secondNode.getId()).isEqualTo("node2");
        assertThat(secondNode.getLabel()).isEqualTo("Second Node");
        assertThat(secondNode.getPorts()).isNotNull();

        // Test first edge (simple source/target)
        CjJacksonEdge firstEdge = doc.getEdges().getFirst();
        assertThat(firstEdge.getSource()).isEqualTo("node1");
        assertThat(firstEdge.getTarget()).isEqualTo("node2");

        // Test second edge (with endpoints)
        CjJacksonEdge secondEdge = doc.getEdges().get(1);
        assertThat(secondEdge.getEndpoints()).isNotNull();
        assertThat(secondEdge.getEndpoints()).hasSize(2);

        // test extended JSON with any props, here 'data' was used
        Map<String, JsonNode> secondEdgeData = secondEdge.getAdditionalProperties();
        assertThat(secondEdgeData).isNotNull();
        JsonNode data = secondEdgeData.get("data");
        assertThat(data.get("foo")).isNotNull();
        assertThat(data.get("foo").asText()).isEqualTo("bar");

        CjJacksonEndpoint firstEndpoint = secondEdge.getEndpoints().getFirst();
        assertThat(firstEndpoint.getDirection()).isEqualTo("out");
        assertThat(firstEndpoint.getNode()).isEqualTo("node1");

        CjJacksonEndpoint secondEndpoint = secondEdge.getEndpoints().get(1);
        assertThat(secondEndpoint.getDirection()).isEqualTo("in");
        assertThat(secondEndpoint.getNode()).isEqualTo("node2");
        assertThat(secondEndpoint.getPort()).isEqualTo("port1");

        // Test subgraph
        CjJacksonGraph subgraph = doc.getGraphs().getFirst();
        assertThat(subgraph.getId()).isEqualTo("subgraph1");
        assertThat(subgraph.getNodes()).isNotNull();
        assertThat(subgraph.getNodes()).hasSize(1);

        // Test custom properties (extensible element)
        assertThat(doc.getAdditionalProperties()).isNotNull();

        System.out.println("[DEBUG_LOG] All assertions passed - Connected JSON parsing works correctly");
    }

}
