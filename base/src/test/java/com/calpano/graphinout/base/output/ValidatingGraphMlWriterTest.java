package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ValidatingGraphMlWriterTest {
    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String NODE_ID_3 = "node3";
    public static final String EDGE_ID_1 = "edge1";
    public static final GraphmlEndpoint ENDPOINT_1 = GraphmlEndpoint.builder().node(NODE_ID_1).build();
    public static final GraphmlEndpoint ENDPOINT_2 = GraphmlEndpoint.builder().node(NODE_ID_2).build();
    private AutoCloseable closeable;
    private ValidatingGraphMlWriter underTest;
    private GraphmlWriter mockGraphMlWriter;
    @Mock
    private GraphmlGraph mockGraph;
    @Mock
    private GraphmlDocument mockDocument;
    @Mock
    private GraphmlLocator mockLocator;
    @Mock
    private GraphmlNode mockNode;
    @Mock
    private GraphmlHyperEdge mockHyperEdge;
    @Mock
    private GraphmlEdge mockEdge;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockGraphMlWriter = mock(GraphmlWriter.class);
        this.underTest = new ValidatingGraphMlWriter(mockGraphMlWriter);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void shouldWorkAsIntendedWithDocument() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.endDocument();

        verify(mockGraphMlWriter).startDocument(mockDocument);
        verify(mockGraphMlWriter).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithGraph() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.endGraph(Optional.of(mockLocator));
        underTest.endDocument();

        verify(mockGraphMlWriter).startDocument(mockDocument);
        verify(mockGraphMlWriter).startGraph(mockGraph);
        verify(mockGraphMlWriter).endGraph(Optional.of(mockLocator));
        verify(mockGraphMlWriter).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithNode() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_1);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        mockNode.setId(NODE_ID_1);
        underTest.startNode(mockNode);
        underTest.endNode(Optional.of(mockLocator));
        underTest.endGraph(Optional.of(mockLocator));
        underTest.endDocument();

        verify(mockGraphMlWriter).startDocument(mockDocument);
        verify(mockGraphMlWriter).startGraph(mockGraph);
        verify(mockGraphMlWriter).startNode(mockNode);
        verify(mockGraphMlWriter).endNode(Optional.of(mockLocator));
        verify(mockGraphMlWriter).endGraph(Optional.of(mockLocator));
        verify(mockGraphMlWriter).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithEdge() throws IOException {
        GraphmlNode mockNode2 = mock(GraphmlNode.class);

        when(mockNode.getId()).thenReturn(NODE_ID_1);
        when(mockNode2.getId()).thenReturn(NODE_ID_2);
        when(mockEdge.getId()).thenReturn(EDGE_ID_1);
        when(mockEdge.getSourceId()).thenReturn(NODE_ID_1);
        when(mockEdge.getTargetId()).thenReturn(NODE_ID_2);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(Optional.of(mockLocator));
        underTest.startNode(mockNode2);
        underTest.endNode(Optional.of(mockLocator));
        underTest.startEdge(mockEdge);
        underTest.endEdge();
        underTest.endGraph(Optional.of(mockLocator));
        underTest.endDocument();

        verify(mockGraphMlWriter).startDocument(mockDocument);
        verify(mockGraphMlWriter).startGraph(mockGraph);
        verify(mockGraphMlWriter).startNode(mockNode);
        verify(mockGraphMlWriter).startNode(mockNode2);
        Mockito.verify(mockGraphMlWriter, Mockito.times(2)).endNode(Optional.of(mockLocator));
        verify(mockGraphMlWriter).startEdge(mockEdge);
        verify(mockGraphMlWriter).endEdge();
        verify(mockGraphMlWriter).endGraph(Optional.of(mockLocator));
        verify(mockGraphMlWriter).endDocument();
    }

    @Test
    void shouldThrowExceptionWhenElementsHaveWrongOrder() throws IOException {
        underTest.startDocument(mockDocument);

        assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode));
    }

    @Test
    void shouldThrowExceptionWhenElementsHaveWrongOrder_2() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);

        assertThrows(IllegalStateException.class, () -> underTest.startHyperEdge(mockHyperEdge));
    }

    @Test
    void shouldThrowExceptionWhenNodeHasNoId() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);

        assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode));
    }

    @Test
    void shouldThrowExceptionWhenNodeIdIsNotUnique() throws IOException {
        GraphmlNode mockNode2 = mock(GraphmlNode.class);

        when(mockNode.getId()).thenReturn(NODE_ID_1);
        when(mockNode2.getId()).thenReturn(NODE_ID_1);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(Optional.of(mockLocator));

        assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode2));
    }

    @Test
    void shouldThrowExceptionWhenHyperEdgeRefersToNonExistingNode() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_3);
        when(mockHyperEdge.getEndpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(Optional.of(mockLocator));
        underTest.startHyperEdge(mockHyperEdge);
        underTest.endHyperEdge();
        underTest.endGraph(Optional.of(mockLocator));

        assertThrows(IllegalStateException.class, () -> underTest.endDocument());
    }

    @Test
    void shouldThrowExceptionWhenEdgeHasNoEndpoints() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_3);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(Optional.of(mockLocator));

        assertThrows(IllegalArgumentException.class, () -> underTest.startEdge(mockEdge));
    }
}