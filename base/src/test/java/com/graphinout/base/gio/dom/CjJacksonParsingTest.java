package com.graphinout.base.gio.dom;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.jackson.CjJacksonDocument;
import com.graphinout.base.cj.jackson.CjJacksonEdge;
import com.graphinout.base.cj.jackson.CjJacksonEndpoint;
import com.graphinout.base.cj.jackson.CjJacksonGraph;
import com.graphinout.base.cj.jackson.CjJacksonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class CjJacksonParsingTest {

    /**
     * A Connected JSON 8.0.0 "graph entry" sample: document-level nodes/edges (incl. a source/target shortcut), a node
     * with ports, an edge with explicit endpoints and {@code data}, and a subgraph. Inlined so the test is
     * self-contained — the on-disk fixture moved to the external graph-test-data repo in the test-data migration.
     */
    private static final String SAMPLE_1_GRAPH_ENTRY = """
            {
              "$schema": "https://j-s-o-n.org/schema/cj-8.0.0.json",
              "nodes": [
                { "id": "node1", "label": "First Node" },
                { "id": "node2", "label": "Second Node", "ports": [ "port1", { "id": "port2" } ] }
              ],
              "edges": [
                { "source": "node1", "target": "node2" },
                {
                  "data": { "foo": "bar" },
                  "endpoints": [
                    { "direction": "out", "node": "node1" },
                    { "direction": "in", "node": "node2", "port": "port1" }
                  ]
                }
              ],
              "graphs": [
                {
                  "id": "subgraph1",
                  "nodes": [ { "id": "sub1", "label": "Sub Node" } ],
                  "edges": [
                    { "endpoints": [ { "direction": "in", "node": "sub1" }, { "direction": "out", "node": "node2" } ] }
                  ]
                }
              ]
            }
            """;

    @Test
    public void testParseConnectedJsonDocument() throws IOException {
        tools.jackson.databind.json.JsonMapper objectMapper = tools.jackson.databind.json.JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        CjJacksonDocument doc = objectMapper.readValue(SAMPLE_1_GRAPH_ENTRY, CjJacksonDocument.class);

        // Test document-level nodes and edges
        assertThat(doc.getNodes()).isNotNull();
        assertThat(doc.getNodes()).hasSize(2);

        assertThat(doc.getEdges()).isNotNull();
        assertThat(doc.getEdges()).hasSize(2);

        // Test graphs
        assertThat(doc.getGraphs()).isNotNull();
        assertThat(doc.getGraphs()).hasSize(1);

        // Test first node
        CjJacksonNode firstNode = doc.getNodes().get(0);
        assertThat(firstNode.getId()).isEqualTo("node1");
        assertThat(firstNode.getLabel()).isEqualTo("First Node");

        // Test second node with ports
        CjJacksonNode secondNode = doc.getNodes().get(1);
        assertThat(secondNode.getId()).isEqualTo("node2");
        assertThat(secondNode.getLabel()).isEqualTo("Second Node");
        assertThat(secondNode.getPorts()).isNotNull();

        // Test first edge (simple source/target)
        CjJacksonEdge firstEdge = doc.getEdges().get(0);
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

        CjJacksonEndpoint firstEndpoint = secondEdge.getEndpoints().get(0);
        assertThat(firstEndpoint.getDirection()).isEqualTo("out");
        assertThat(firstEndpoint.getNode()).isEqualTo("node1");

        CjJacksonEndpoint secondEndpoint = secondEdge.getEndpoints().get(1);
        assertThat(secondEndpoint.getDirection()).isEqualTo("in");
        assertThat(secondEndpoint.getNode()).isEqualTo("node2");
        assertThat(secondEndpoint.getPort()).isEqualTo("port1");

        // Test subgraph
        CjJacksonGraph subgraph = doc.getGraphs().get(0);
        assertThat(subgraph.getId()).isEqualTo("subgraph1");
        assertThat(subgraph.getNodes()).isNotNull();
        assertThat(subgraph.getNodes()).hasSize(1);

        // Test custom properties (extensible element)
        assertThat(doc.getAdditionalProperties()).isNotNull();

        System.out.println("[DEBUG_LOG] All assertions passed - Connected JSON parsing works correctly");
    }

}
