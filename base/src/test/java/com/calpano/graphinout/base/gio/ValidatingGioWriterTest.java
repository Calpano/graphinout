package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ValidatingGioWriterTest {
    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String NODE_ID_3 = "node3";
    public static final String EDGE_ID_1 = "edge1";
    public static final String KEY_ID_1 = "key1";

    public static final GioEndpoint ENDPOINT_1 = GioEndpoint.builder().node(NODE_ID_1).build();
    public static final GioEndpoint ENDPOINT_2 = GioEndpoint.builder().node(NODE_ID_2).build();
    private AutoCloseable closeable;
    private DelegatingGioWriter underTest;
    @Mock
    private GioWriterImpl mockGioWriterImpl;
    @Mock
    private GioKey mockKey;
    @Mock
    private GioDocument mockDocument;
    @Mock
    private GioGraph mockGraph;
    @Mock
    private GioEdge mockEdge;
    @Mock
    private GioNode mockNode;
    @Mock
    private URL mockLocator;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        underTest = new DelegatingGioWriter(new ValidatingGioWriter(), mockGioWriterImpl);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldWorkAsIntendedWithDocument() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.endDocument();
        verify(mockGioWriterImpl).startDocument(mockDocument);
        verify(mockGioWriterImpl).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithGraph() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.endGraph(mockLocator);
        underTest.endDocument();

        verify(mockGioWriterImpl).startDocument(mockDocument);
        verify(mockGioWriterImpl).startGraph(mockGraph);
        verify(mockGioWriterImpl).endGraph(mockLocator);
        verify(mockGioWriterImpl).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithNode() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_1);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        mockNode.setId(NODE_ID_1);
        underTest.startNode(mockNode);
        underTest.endNode(mockLocator);
        underTest.endGraph(mockLocator);
        underTest.endDocument();

        verify(mockGioWriterImpl).startDocument(mockDocument);
        verify(mockGioWriterImpl).startGraph(mockGraph);
        verify(mockGioWriterImpl).startNode(mockNode);
        verify(mockGioWriterImpl).endNode(mockLocator);
        verify(mockGioWriterImpl).endGraph(mockLocator);
        verify(mockGioWriterImpl).endDocument();
    }

    @Test
    void shouldWorkAsIntendedWithEdge() throws IOException {
        GioNode mockNode2 = mock(GioNode.class);
        when(mockNode.getId()).thenReturn(NODE_ID_1);
        when(mockNode2.getId()).thenReturn(NODE_ID_2);
        when(mockEdge.getId()).thenReturn(EDGE_ID_1);
        when(mockEdge.getEndpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(mockLocator);
        underTest.startNode(mockNode2);
        underTest.endNode(mockLocator);
        underTest.startEdge(mockEdge);
        underTest.endEdge();
        underTest.endGraph(mockLocator);
        underTest.endDocument();

        verify(mockGioWriterImpl).startDocument(mockDocument);
        verify(mockGioWriterImpl).startGraph(mockGraph);
        verify(mockGioWriterImpl).startNode(mockNode);
        verify(mockGioWriterImpl).startNode(mockNode2);
        Mockito.verify(mockGioWriterImpl, Mockito.times(2)).endNode(mockLocator);
        verify(mockGioWriterImpl).startEdge(mockEdge);
        verify(mockGioWriterImpl).endEdge();
        verify(mockGioWriterImpl).endGraph(mockLocator);
        verify(mockGioWriterImpl).endDocument();
    }

    @Test
    void shouldThrowExceptionWhenNodeHasNoId() throws IOException {
        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);

        assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode));
    }

    @Test
    void shouldThrowExceptionWhenNodeIdIsNotUnique() throws IOException {
        GioNode mockNode2 = mock(GioNode.class);
        when(mockNode.getId()).thenReturn(NODE_ID_1);
        when(mockNode2.getId()).thenReturn(NODE_ID_1);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(mockLocator);

        assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode2));
    }

    @Test
    void shouldThrowExceptionWhenHyperEdgeRefersToNonExistingNode() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_3);
        when(mockEdge.getEndpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(mockLocator);
        underTest.startEdge(mockEdge);
        underTest.endEdge();

        assertThrows(IllegalStateException.class, () -> underTest.endGraph(mockLocator));
    }

    @Test
    void shouldThrowExceptionWhenEdgeHasNoEndpoints() throws IOException {
        when(mockNode.getId()).thenReturn(NODE_ID_3);

        underTest.startDocument(mockDocument);
        underTest.startGraph(mockGraph);
        underTest.startNode(mockNode);
        underTest.endNode(mockLocator);

        assertThrows(IllegalStateException.class, () -> underTest.startEdge(mockEdge));
    }

    @Test
    void shouldThrowExceptionWhenKeyIsNotUnique() throws IOException {
        GioKey mockKey2 = mock(GioKey.class);
        when(mockKey.getId()).thenReturn(KEY_ID_1);
        when(mockKey2.getId()).thenReturn(KEY_ID_1);

        underTest.key(mockKey);

        assertThrows(IllegalStateException.class, () -> underTest.key(mockKey2));
    }
}
