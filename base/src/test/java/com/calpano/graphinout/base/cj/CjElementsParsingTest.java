package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.stream.util.DelegatingCjWriter;
import com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.json.stream.impl.DelegatingJsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.LoggingJsonWriter;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

public class CjElementsParsingTest {

    static ICjDocument toCjDoc(String resourceName) throws IOException {
        // load resource "sample-1.cj.json"
        // Use ClassLoader to get the resource URL
        java.net.URL resourceUrl = CjElementsParsingTest.class.getClassLoader().getResource(resourceName);
        assertThat(resourceUrl).isNotNull();
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);

        Cj2ElementsWriter cj2elements = new Cj2ElementsWriter();
        DelegatingCjWriter cjWriter = new DelegatingCjWriter(cj2elements);
        cjWriter.addCjWriter(new LoggingCjWriter(false));
        SingleInputSource singleInputSource = SingleInputSource.of(resourceUrl.toString(), content);

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        Json2CjWriter sink = new Json2CjWriter(cjWriter);
        DelegatingJsonWriter jsonWriter = new DelegatingJsonWriter(new LoggingJsonWriter(LoggingJsonWriter.Output.SystemOut));
        jsonWriter.addJsonWriter(sink);
        jsonReader.read(singleInputSource, jsonWriter);

        return cj2elements.resultDoc();
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/cj/canonical/sample-3.cj.canonical.json",
            "json/cj/canonical/sample-2.cj.canonical.json",
            "json/cj/canonical/sample-2b.cj.canonical.json",
            "json/cj/canonical/sample-1b.cj.canonical.json",
            "json/cj/canonical/sample-1.cj.canonical.json"
    })
    public void testParse(String resourceName) throws IOException {
        ICjDocument doc = toCjDoc(resourceName);
        assertThat(doc).isNotNull();
    }

    @Test
    public void testParseConnectedJsonDocument() throws IOException {
        ICjDocument doc = toCjDoc("json/cj/canonical/sample-1.cj.canonical.json");
        // Test document-level nodes and edges
        ICjGraph graph_0 = doc.graphs().findFirst().orElseThrow();

        assertThat(graph_0.nodes()).isNotNull();
        assertThat(graph_0.nodes()).hasSize(2);

        assertThat(graph_0.edges()).isNotNull();
        assertThat(graph_0.edges()).hasSize(2);

        // Test graphs
        assertThat(graph_0.graphs()).isNotNull();
        assertThat(graph_0.graphs()).hasSize(1);

        // Test first node
        ICjNode firstNode = graph_0.nodes().findFirst().orElseThrow();
        assertThat(firstNode.id()).isEqualTo("node1");
        assertThat(firstNode.labelEntries().getFirst().value()).isEqualTo("First Node");

        // Test second node with ports
        ICjNode secondNode = graph_0.nodes().skip(1).findFirst().orElseThrow();
        assertThat(secondNode.id()).isEqualTo("node2");
        assertThat(secondNode.labelEntries().getFirst().value()).isEqualTo("Second Node");
        assertThat(secondNode.ports()).isNotNull();

        // Test first edge (simple source/target)
        ICjEdge firstEdge = graph_0.edges().findFirst().orElseThrow();
        ICjEndpoint source = firstEdge.source();
        assertThat(source).isNotNull();
        assertThat(source.node()).isEqualTo("node1");
        ICjEndpoint target = firstEdge.target();
        assertThat(target).isNotNull();
        assertThat(target.node()).isEqualTo("node2");

        // Test second edge (with endpoints)
        ICjEdge secondEdge = graph_0.edges().skip(1).findFirst().orElseThrow();
        assertThat(secondEdge.endpoints()).isNotNull();
        assertThat(secondEdge.endpoints()).hasSize(2);

        // test extended JSON with any props, here 'data' was used
        ICjData secondEdgeData = secondEdge.data();
        assertThat(secondEdgeData).isNotNull();
        IJsonValue secondEdgeJsonValue = secondEdgeData.jsonValue();
        assertThat(secondEdgeJsonValue).isNotNull();
        IJsonValue secondEdgeJson_foo = secondEdgeJsonValue.asObject().get("foo");
        assertThat(secondEdgeJson_foo).isNotNull();
        assertThat(secondEdgeJson_foo.base()).isEqualTo("bar");

        ICjEndpoint firstEndpoint = secondEdge.endpoints().findFirst().orElseThrow();
        assertThat(firstEndpoint.direction()).isEqualTo(CjDirection.OUT);
        assertThat(firstEndpoint.node()).isEqualTo("node1");

        ICjEndpoint secondEndpoint = secondEdge.endpoints().skip(1).findFirst().orElseThrow();
        assertThat(secondEndpoint.direction()).isEqualTo(CjDirection.IN);
        assertThat(secondEndpoint.node()).isEqualTo("node2");
        assertThat(secondEndpoint.port()).isEqualTo("port1");

        // Test subgraph
        ICjGraph graph = doc.graphs().findFirst().orElseThrow();
        ICjGraph subgraph = graph.graphs().findFirst().orElseThrow();
        assertThat(subgraph.id()).isEqualTo("subgraph1");
        assertThat(subgraph.nodes()).isNotNull();
        assertThat(subgraph.nodes()).hasSize(1);
    }

}
