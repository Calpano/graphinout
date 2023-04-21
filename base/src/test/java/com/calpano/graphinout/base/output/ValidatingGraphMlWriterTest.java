package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.graphml.*;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter.CurrentElement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidatingGraphMlWriterTest {
    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String NODE_ID_3 = "node3";
    public static final String EDGE_ID_1 = "edge1";
    public static final String PORT_NAME_1 ="port_name_1";
    public static final GraphmlEndpoint ENDPOINT_1 = GraphmlEndpoint.builder().node(NODE_ID_1).build();
    public static final GraphmlEndpoint ENDPOINT_2 = GraphmlEndpoint.builder().node(NODE_ID_2).build();
    private AutoCloseable closeable;
    private DelegatingGraphmlWriter underTest;
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
        @Mock
        private GraphmlKey mockKey;

        @BeforeEach
        void setUp() throws MalformedURLException {
            closeable = MockitoAnnotations.openMocks(this);
            mockGraphMlWriter = mock(GraphmlWriter.class);
            underTest = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), mockGraphMlWriter);
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
            assertAll("",//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),//
                    () -> verifyNoMoreInteractions(mockGraphMlWriter));
        }

        @Test
        void shouldWorkAsIntendedWithGraph() throws IOException {

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.endGraph(Optional.of(mockLocator));
            underTest.endDocument();

            assertAll("",//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),//
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


            assertAll("",//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),//
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
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode2),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(graphmlLocator2)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startEdge(mockEdge),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endEdge(),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endDocument(),//
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
        @Mock
        private GraphmlKey mockKey;
        @Mock
        private GraphmlData mockData;
        @Mock
        private GraphmlPort mockPort;



        @BeforeEach
        void setUp() {
            closeable = MockitoAnnotations.openMocks(this);
            mockGraphMlWriter = mock(GraphmlWriter.class);
            underTest = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), mockGraphMlWriter);
            inOrder = Mockito.inOrder(mockGraphMlWriter);
        }

        @AfterEach
        public void releaseMocks() throws Exception {
            closeable.close();
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
        void shouldThrowExceptionWhenPortNameIsNotUnique() throws IOException {
            GraphmlNode mockNode2 = mock(GraphmlNode.class);
            when(mockLocator.getXLinkHref()).thenReturn(URI.create("http://example.com").toURL());
            when(mockNode.getId()).thenReturn(NODE_ID_1);
            when(mockPort.getName()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.startPort(mockPort);
            Exception exception = assertThrows(Exception.class,
                    () -> underTest.startPort(mockPort));

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class,exception),//
                    () -> assertEquals("Port must have a unique Name, but name is used several times: 'port_name_1'.", exception.getMessage()));

        }

        @Test
        void shouldThrowExceptionWhenPortRefersNonExisting() throws IOException {
            when(mockNode.getId()).thenReturn(NODE_ID_1);
            GraphmlEndpoint ENDPOINT_TEST_PORT = GraphmlEndpoint.builder().node(NODE_ID_1).port("123").build();
            when(mockEdge.getSourceId()).thenReturn(NODE_ID_1);
            when(mockEdge.getTargetId()).thenReturn(NODE_ID_1);
            when(mockEdge.getSourcePortId()).thenReturn("123");
            when(mockEdge.getTargetPortId()).thenReturn("456");
            when(mockPort.getName()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);

            underTest.startNode(mockNode);
            underTest.startPort(mockPort);
            underTest.endPort();
            underTest.endNode(Optional.empty());
            underTest.startEdge(mockEdge);
            underTest.endEdge();
            underTest.endGraph(Optional.empty());


            Exception exception = assertThrows(Exception.class,
                    () -> underTest.endDocument());

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class,exception),//
                    () -> assertEquals("2 ports used in the graph without reference.", exception.getMessage()));

        }
        @Test
        void shouldThrowExceptionWhenPortRefersNonExistingInHyperEdge() throws IOException {
            when(mockNode.getId()).thenReturn(NODE_ID_1);
            GraphmlEndpoint ENDPOINT_TEST_PORT = GraphmlEndpoint.builder().node(NODE_ID_1).port("123").build();
            when(mockHyperEdge.getEndpoints()).thenReturn(List.of(ENDPOINT_TEST_PORT, ENDPOINT_TEST_PORT));
            when(mockPort.getName()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);

            underTest.startNode(mockNode);
            underTest.startPort(mockPort);
            underTest.endPort();
            underTest.endNode(Optional.empty());
            underTest.startHyperEdge(mockHyperEdge);
            underTest.endHyperEdge();
            underTest.endGraph(Optional.empty());


            Exception exception = assertThrows(Exception.class,
                    () -> underTest.endDocument());

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class,exception),//
                    () -> assertEquals("1 ports used in the graph without reference.", exception.getMessage()));

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
            assertAll("",//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startDocument(mockDocument),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startGraph(mockGraph),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startNode(mockNode),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endNode(Optional.of(mockLocator)),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).startHyperEdge(mockHyperEdge),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endHyperEdge(),//
                    () -> inOrder.verify(mockGraphMlWriter, times(1)).endGraph(Optional.of(mockLocator)),//
                    () -> verifyNoMoreInteractions(mockGraphMlWriter),//
                    () -> assertEquals("2 nodes used in the graph without reference.", illegalStateException.getMessage()));

        }

        private static Stream<ParameterData> parameterDataList() throws IOException {

            List<ParameterData> parameterDataList = new ArrayList<>();
            parameterDataList.add(new ParameterData( List.of(), "key", CurrentElement.EMPTY, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "data", CurrentElement.EMPTY, CurrentElement.DATA, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "startGraph", CurrentElement.EMPTY, CurrentElement.GRAPH, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "startNode", CurrentElement.EMPTY, CurrentElement.NODE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "startEdge", CurrentElement.EMPTY, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "startHyperEdge", CurrentElement.EMPTY, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "startPort", CurrentElement.EMPTY, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of(), "endGraph", CurrentElement.GRAPH, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of(), "endNode", CurrentElement.NODE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of(), "endEdge", CurrentElement.EDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            //10
            parameterDataList.add(new ParameterData( List.of(), "endEdge", CurrentElement.EDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of(), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of(), "endPort", CurrentElement.PORT, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "startNode", CurrentElement.GRAPHML, CurrentElement.NODE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "startEdge", CurrentElement.GRAPHML, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "startHyperEdge", CurrentElement.GRAPHML, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "startPort", CurrentElement.GRAPHML, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "endGraph", CurrentElement.GRAPH, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "endNode", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "endEdge", CurrentElement.EDGE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            //20
            parameterDataList.add(new ParameterData( List.of("startDocument"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument"), "endPort", CurrentElement.PORT, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "startDocument", CurrentElement.GRAPH, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "key", CurrentElement.GRAPH, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "endDocument", CurrentElement.GRAPHML, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "endNode", CurrentElement.NODE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "endEdge", CurrentElement.EDGE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph"), "endPort", CurrentElement.PORT, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "startDocument", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            //30
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "key", CurrentElement.NODE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "startDocument", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "endDocument", CurrentElement.GRAPHML, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "endGraph", CurrentElement.GRAPH, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "endEdge", CurrentElement.EDGE, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode"), "endPort", CurrentElement.PORT, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startEdge"), "key", CurrentElement.EDGE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startEdge"), "endDocument", CurrentElement.GRAPHML, CurrentElement.EDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startEdge"), "endGraph", CurrentElement.GRAPH, CurrentElement.EDGE, GraphmlWriterEndException.class));
            //40
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startEdge"), "endNode", CurrentElement.NODE, CurrentElement.EDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "endDocument", CurrentElement.GRAPHML, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "startDocument", CurrentElement.HYPEREDGE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "startEdge", CurrentElement.HYPEREDGE, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "startPort", CurrentElement.HYPEREDGE, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "key", CurrentElement.HYPEREDGE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "endGraph", CurrentElement.GRAPH, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "endNode", CurrentElement.NODE, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "endEdge", CurrentElement.EDGE, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startHyperEdge"), "endPort", CurrentElement.PORT, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            //50
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "endDocument", CurrentElement.GRAPHML, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "startDocument", CurrentElement.PORT, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "startEdge", CurrentElement.PORT, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "startHyperEdge", CurrentElement.PORT, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "key", CurrentElement.PORT, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "endGraph", CurrentElement.GRAPH, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "endNode", CurrentElement.NODE, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "endEdge", CurrentElement.EDGE, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData( List.of("startDocument", "startGraph", "startNode", "startPort"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.PORT, GraphmlWriterEndException.class));

            return parameterDataList.stream();
        }

        @ParameterizedTest
        @MethodSource("parameterDataList")
        void shouldThrowException(ParameterData parameterData) throws Exception {
            // TODO remove mockGraphmlWriter, now that we changed our delegation strategy to proxy pattern
            underTest = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), mockGraphMlWriter);
            for (String t : parameterData.previousOrders) {
                execute(t);
            }

            Exception exception = assertThrows(Exception.class, () -> execute(parameterData.exceptionThrowerMethod));
            assertAll("",//
                    () -> assertInstanceOf(parameterData.exceptionClass, exception),//
                    () -> assertEquals(parameterData.offendingElement, ((GraphmlWriterException) exception).offendingElement),
                    () -> assertEquals(parameterData.lastStartedElement, ((GraphmlWriterException) exception).lastStartedElement)
            );
        }

        private void execute(String methodName) throws IOException {

            switch (methodName) {
                case "key":
                    underTest.key(mockKey);
                    break;
                case "data":
                    underTest.data(mockData);
                    break;
                case "startDocument":
                    underTest.startDocument(mockDocument);
                    break;
                case "startGraph":
                    underTest.startGraph(mockGraph);
                    break;
                case "startNode":
                    when(mockNode.getId()).thenReturn(NODE_ID_1);
                    underTest.startNode(mockNode);
                    break;
                case "startEdge":
                    when(mockEdge.getSourceId()).thenReturn(NODE_ID_1);
                    when(mockEdge.getTargetId()).thenReturn(NODE_ID_2);
                    underTest.startEdge(mockEdge);
                    break;
                case "startHyperEdge":
                    when(mockHyperEdge.getEndpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));
                    underTest.startHyperEdge(mockHyperEdge);
                    break;
                case "startPort":
                    when(mockPort.getName()).thenReturn(PORT_NAME_1);
                    underTest.startPort(mockPort);
                    break;
                case "endDocument":
                    underTest.endDocument();
                    break;
                case "endGraph":
                    underTest.endGraph(Optional.empty());
                    break;
                case "endNode":
                    underTest.endNode(Optional.empty());
                    break;
                case "endEdge":
                    underTest.endEdge();
                    break;
                case "endHyperEdge":
                    underTest.endHyperEdge();
                    break;
                case "endPort":
                    underTest.endPort();
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Method <%s> not supported.", methodName));
            }

        }

        static class ParameterData {
            List<String> previousOrders;
            String exceptionThrowerMethod;
            ValidatingGraphMlWriter.CurrentElement offendingElement;
            ValidatingGraphMlWriter.CurrentElement lastStartedElement;

            Class exceptionClass;

            public ParameterData(List<String> previousOrders, String exceptionThrowerMethod, CurrentElement offendingElement, CurrentElement lastStartedElement, Class exceptionClass) {
                this.previousOrders = previousOrders;
                this.exceptionThrowerMethod = exceptionThrowerMethod;
                this.offendingElement = offendingElement;
                this.lastStartedElement = lastStartedElement;
                this.exceptionClass = exceptionClass;
            }

            public ParameterData(String[] previousOrders, String exceptionThrowerMethod, ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, Class exceptionClass) {
                this(List.of(previousOrders), exceptionThrowerMethod, offendingElement, lastStartedElement, exceptionClass);
            }

        }

    }
}
