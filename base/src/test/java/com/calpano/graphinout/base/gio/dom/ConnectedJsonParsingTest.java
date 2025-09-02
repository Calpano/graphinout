package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConnectedJsonParsingTest {

    @Test
    public void testParseConnectedJsonDocument() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        // load resource "sample-1.cj.json"
        // Use ClassLoader to get the resource URL
        java.net.URL resourceUrl = getClass().getClassLoader().getResource("sample-1.cj.json");
        assertNotNull(resourceUrl, "Resource sample-1.cj.json not found.");
        File jsonFile = new File(resourceUrl.getFile());
        GioDocument doc = objectMapper.readValue(jsonFile, GioDocument.class);

        System.out.println("[DEBUG_LOG] Parsed GioDocument successfully");

        // Test document-level nodes and edges
        assertNotNull(doc.getNodes(), "Document should have nodes");
        assertEquals(2, doc.getNodes().size(), "Document should have 2 nodes");

        assertNotNull(doc.getEdges(), "Document should have edges");
        assertEquals(2, doc.getEdges().size(), "Document should have 2 edges");

        // Test graphs
        assertNotNull(doc.getGraphs(), "Document should have graphs");
        assertEquals(1, doc.getGraphs().size(), "Document should have 1 graph");

        // Test first node
        GioNode firstNode = doc.getNodes().get(0);
        assertEquals("node1", firstNode.getId());
        assertEquals("First Node", firstNode.getLabel());

        // Test second node with ports
        GioNode secondNode = doc.getNodes().get(1);
        assertEquals("node2", secondNode.getId());
        assertEquals("Second Node", secondNode.getLabel());
        assertNotNull(secondNode.getPorts(), "Second node should have ports");

        // Test first edge (simple source/target)
        GioEdge firstEdge = doc.getEdges().get(0);
        assertEquals("node1", firstEdge.getSource());
        assertEquals("node2", firstEdge.getTarget());

        // Test second edge (with endpoints)
        GioEdge secondEdge = doc.getEdges().get(1);
        assertNotNull(secondEdge.getEndpoints(), "Second edge should have endpoints");
        assertEquals(2, secondEdge.getEndpoints().size(), "Second edge should have 2 endpoints");

        GioEndpoint firstEndpoint = secondEdge.getEndpoints().get(0);
        assertEquals("out", firstEndpoint.getDirection());
        assertEquals("node1", firstEndpoint.getNode());

        GioEndpoint secondEndpoint = secondEdge.getEndpoints().get(1);
        assertEquals("in", secondEndpoint.getDirection());
        assertEquals("node2", secondEndpoint.getNode());
        assertEquals("port1", secondEndpoint.getPort());

        // Test subgraph
        GioGraph subgraph = doc.getGraphs().get(0);
        assertEquals("subgraph1", subgraph.getId());
        assertNotNull(subgraph.getNodes(), "Subgraph should have nodes");
        assertEquals(1, subgraph.getNodes().size(), "Subgraph should have 1 node");

        // Test custom properties (extensible element)
        assertNotNull(doc.getAdditionalProperties(), "Document should have additional properties");

        System.out.println("[DEBUG_LOG] All assertions passed - Connected JSON parsing works correctly");
    }

}
