package com.graphinout.base.gio;

import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.writer.ValidatingCjStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatingCjStreamTest {

    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String EDGE_ID_1 = "edge1";

    private ValidatingCjStream underTest;

    @BeforeEach
    void setUp() {
        underTest = new ValidatingCjStream();
    }

    @Test
    void shouldAcceptValidBaseUri() throws IOException {
        String validUri = "http://example.com/valid/uri";

        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        doc.baseUri(validUri);
        underTest.documentStart(doc);
        underTest.documentEnd();
    }

    @Test
    void shouldAllowNodeWithoutId() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjNodeChunkMutable node = underTest.createNodeChunk(); // no id

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.nodeStart(node); // should not throw
        underTest.nodeEnd();
        underTest.graphEnd();
        underTest.documentEnd();
    }

    @Test
    void shouldNotThrowWhenEdgeHasNoEndpoints() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjNodeChunkMutable node = underTest.createNodeChunk();
        node.id(NODE_ID_1);
        ICjEdgeChunkMutable edge = underTest.createEdgeChunk(); // no endpoints

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.nodeStart(node);
        underTest.nodeEnd();
        underTest.edgeStart(edge);
        underTest.edgeEnd();
        underTest.graphEnd();
        underTest.documentEnd();
    }

    @Test
    void shouldThrowExceptionWhenBaseUriIsInvalid() throws IOException {
        String invalidUri = "invalid:///uri with spaces";

        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        doc.baseUri(invalidUri);

        assertThrows(IllegalStateException.class, () -> underTest.documentStart(doc));
    }

    @Test
    void shouldThrowExceptionWhenHyperEdgeRefersToNonExistingNode() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjEdgeChunkMutable edge = underTest.createEdgeChunk();
        edge.createEndpoint(ep -> ep.node(NODE_ID_1));
        edge.addEndpoint(ep -> ep.node(NODE_ID_2));

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.edgeStart(edge);
        underTest.edgeEnd();

        assertThrows(IllegalStateException.class, () -> underTest.graphEnd());
    }

    @Test
    void shouldThrowExceptionWhenNodeIdIsNotUnique() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjNodeChunkMutable node1 = underTest.createNodeChunk();
        node1.id(NODE_ID_1);
        ICjNodeChunkMutable node2 = underTest.createNodeChunk();
        node2.id(NODE_ID_1);

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.nodeStart(node1);
        underTest.nodeEnd();

        assertThrows(IllegalStateException.class, () -> underTest.nodeStart(node2));
    }

    @Test
    void shouldWorkAsIntendedWithDocument() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        underTest.documentStart(doc);
        underTest.documentEnd();
    }

    @Test
    void shouldWorkAsIntendedWithEdge() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjNodeChunkMutable node1 = underTest.createNodeChunk();
        node1.id(NODE_ID_1);
        ICjNodeChunkMutable node2 = underTest.createNodeChunk();
        node2.id(NODE_ID_2);
        ICjEdgeChunkMutable edge = underTest.createEdgeChunk();
        edge.id(EDGE_ID_1);
        edge.createEndpoint(ep -> ep.node(NODE_ID_1));
        edge.addEndpoint(ep -> ep.node(NODE_ID_2));

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.nodeStart(node1);
        underTest.nodeEnd();
        underTest.nodeStart(node2);
        underTest.nodeEnd();
        underTest.edgeStart(edge);
        underTest.edgeEnd();
        underTest.graphEnd();
        underTest.documentEnd();
    }

    @Test
    void shouldWorkAsIntendedWithGraph() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.graphEnd();
        underTest.documentEnd();
    }

    @Test
    void shouldWorkAsIntendedWithNode() {
        ICjDocumentChunkMutable doc = underTest.createDocumentChunk();
        ICjGraphChunkMutable graph = underTest.createGraphChunk();
        ICjNodeChunkMutable node = underTest.createNodeChunk();
        node.id(NODE_ID_1);

        underTest.documentStart(doc);
        underTest.graphStart(graph);
        underTest.nodeStart(node);
        underTest.nodeEnd();
        underTest.graphEnd();
        underTest.documentEnd();
    }

}
