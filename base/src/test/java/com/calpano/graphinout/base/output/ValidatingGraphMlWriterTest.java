package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private InOrder inOrder;

    @DisplayName("The GraphmlWriter test successfully pass.")
    @Nested
    class successfulTest {

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
        void setUp() throws MalformedURLException {
            closeable = MockitoAnnotations.openMocks(this);
            mockGraphMlWriter = mock(GraphmlWriter.class);
            underTest = new ValidatingGraphMlWriter(mockGraphMlWriter);
            inOrder = Mockito.inOrder(mockGraphMlWriter);
            when(mockLocator.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());


        }

        @AfterEach
        public void releaseMocks() throws Exception {
            closeable.close();
        }

        @Test
        void shouldWorkAsIntendedWithDocument() throws IOException {
            underTest.startDocument(mockDocument);
            underTest.endDocument();
            assertAll("",
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),
                    () -> verifyNoMoreInteractions(mockGraphMlWriter));
        }

        @Test
        void shouldWorkAsIntendedWithGraph() throws IOException {

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.endGraph(Optional.of(mockLocator));
            underTest.endDocument();

            assertAll("",
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),
                    () -> verifyNoMoreInteractions(mockGraphMlWriter));
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


            assertAll("",
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),
                    () -> verifyNoMoreInteractions(mockGraphMlWriter));
        }

        @Test
        void shouldWorkAsIntendedWithEdge() throws IOException {
            GraphmlNode mockNode2 = mock(GraphmlNode.class);
            GraphmlLocator graphmlLocator2 = mock(GraphmlLocator.class);

            when(mockNode.getId()).thenReturn(NODE_ID_1);
            when(mockNode2.getId()).thenReturn(NODE_ID_2);
            when(mockEdge.getId()).thenReturn(EDGE_ID_1);
            when(mockEdge.getSourceId()).thenReturn(NODE_ID_1);
            when(mockEdge.getTargetId()).thenReturn(NODE_ID_2);
            when(graphmlLocator2.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(Optional.of(mockLocator));
            underTest.startNode(mockNode2);
            underTest.endNode(Optional.of(graphmlLocator2));
            underTest.startEdge(mockEdge);
            underTest.endEdge();
            underTest.endGraph(Optional.of(mockLocator));
            underTest.endDocument();

            assertAll("",
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode2),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(graphmlLocator2)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startEdge(mockEdge),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endEdge(),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),
                    () -> verifyNoMoreInteractions(mockGraphMlWriter));

        }
    }

    @DisplayName("Graphmlwriter test should not  pass successfully.")
    @Nested
    class notPassTest {
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
            underTest = new ValidatingGraphMlWriter(mockGraphMlWriter);
            inOrder = Mockito.inOrder(mockGraphMlWriter);
        }

        @AfterEach
        public void releaseMocks() throws Exception {
            closeable.close();
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
            when(mockLocator.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());
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
            when(mockLocator.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(Optional.of(mockLocator));
            underTest.startHyperEdge(mockHyperEdge);
            underTest.endHyperEdge();
            underTest.endGraph(Optional.of(mockLocator));

            IllegalStateException illegalStateException = assertThrowsExactly(IllegalStateException.class,
                    () -> underTest.endDocument());
            assertAll("",
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startHyperEdge(mockHyperEdge),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endHyperEdge(),
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),
                    () -> verifyNoMoreInteractions(mockGraphMlWriter),
                    () -> assertEquals("2 nodes used in the graph without reference.", illegalStateException.getMessage()));

        }

        @Test
        void shouldThrowExceptionWhenEdgeHasNoEndpoints() throws IOException {
            when(mockNode.getId()).thenReturn(NODE_ID_3);
            when(mockLocator.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(Optional.of(mockLocator));

            assertThrows(IllegalArgumentException.class, () -> underTest.startEdge(mockEdge));
        }
    }
}