package com.calpano.graphinout.base.gio.dom;

import com.calpano.graphinout.base.cj.jackson.CjDocument;
import com.calpano.graphinout.base.cj.jackson.CjEdge;
import com.calpano.graphinout.base.cj.jackson.CjEndpoint;
import com.calpano.graphinout.base.cj.jackson.CjGraph;
import com.calpano.graphinout.base.cj.jackson.CjNode;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class ConnectedJsonParsingTest {

    @Test
    public void testParseConnectedJsonDocument() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // load resource "sample-1.cj.json"
        // Use ClassLoader to get the resource URL
        java.net.URL resourceUrl = getClass().getClassLoader().getResource("sample-1.cj.json");
        assertThat(resourceUrl).isNotNull();
        File jsonFile = new File(resourceUrl.getFile());
        CjDocument doc = objectMapper.readValue(jsonFile, CjDocument.class);

        // Test document-level nodes and edges
        assertThat(doc.getNodes()).isNotNull();
        assertThat(doc.getNodes()).hasSize(2);

        assertThat(doc.getEdges()).isNotNull();
        assertThat(doc.getEdges()).hasSize(2);

        // Test graphs
        assertThat(doc.getGraphs()).isNotNull();
        assertThat(doc.getGraphs()).hasSize(1);

        // Test first node
        CjNode firstNode = doc.getNodes().getFirst();
        assertThat(firstNode.getId()).isEqualTo("node1");
        assertThat(firstNode.getLabel()).isEqualTo("First Node");

        // Test second node with ports
        CjNode secondNode = doc.getNodes().get(1);
        assertThat(secondNode.getId()).isEqualTo("node2");
        assertThat(secondNode.getLabel()).isEqualTo("Second Node");
        assertThat(secondNode.getPorts()).isNotNull();

        // Test first edge (simple source/target)
        CjEdge firstEdge = doc.getEdges().getFirst();
        assertThat(firstEdge.getSource()).isEqualTo("node1");
        assertThat(firstEdge.getTarget()).isEqualTo("node2");

        // Test second edge (with endpoints)
        CjEdge secondEdge = doc.getEdges().get(1);
        assertThat(secondEdge.getEndpoints()).isNotNull();
        assertThat(secondEdge.getEndpoints()).hasSize(2);

        // test extended JSON with any props, here 'data' was used
        assertThat(secondEdge.getAdditionalProperties()).containsKey("data");

        CjEndpoint firstEndpoint = secondEdge.getEndpoints().getFirst();
        assertThat(firstEndpoint.getDirection()).isEqualTo("out");
        assertThat(firstEndpoint.getNode()).isEqualTo("node1");

        CjEndpoint secondEndpoint = secondEdge.getEndpoints().get(1);
        assertThat(secondEndpoint.getDirection()).isEqualTo("in");
        assertThat(secondEndpoint.getNode()).isEqualTo("node2");
        assertThat(secondEndpoint.getPort()).isEqualTo("port1");

        // Test subgraph
        CjGraph subgraph = doc.getGraphs().getFirst();
        assertThat(subgraph.getId()).isEqualTo("subgraph1");
        assertThat(subgraph.getNodes()).isNotNull();
        assertThat(subgraph.getNodes()).hasSize(1);

        // Test custom properties (extensible element)
        assertThat(doc.getAdditionalProperties()).isNotNull();

        System.out.println("[DEBUG_LOG] All assertions passed - Connected JSON parsing works correctly");
    }

}
