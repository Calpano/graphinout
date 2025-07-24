package com.calpano.graphinout.foundation.output;

import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlLocator;
import com.calpano.graphinout.base.graphml.impl.GraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.validation.GraphmlWriterEndException;
import com.calpano.graphinout.base.validation.GraphmlWriterException;
import com.calpano.graphinout.base.validation.GraphmlWriterStartException;
import com.calpano.graphinout.base.validation.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.validation.ValidatingGraphMlWriter.CurrentElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatingGraphMlWriterTest {

    @DisplayName("The GraphmlWriter test successfully pass.")
    @Nested
    class successfulTest {

        @Test
        void shouldWorkAsIntendedWithDocument() throws IOException {
            underTest.startDocument(mockDocument);
            underTest.endDocument();
        }

        @Test
        void shouldWorkAsIntendedWithEdge() throws IOException {
            GraphmlNode mockNode2 = mock(GraphmlNode.class);
            IGraphmlLocator IGraphmlLocator2 = mock(GraphmlLocator.class);

            when(mockNode.id()).thenReturn(NODE_ID_1);
            when(mockNode2.id()).thenReturn(NODE_ID_2);
            when(mockEdge.id()).thenReturn(EDGE_ID_1);
            when(mockEdge.source()).thenReturn(NODE_ID_1);
            when(mockEdge.target()).thenReturn(NODE_ID_2);
            when(IGraphmlLocator2.xlinkHref()).thenReturn(URI.create("http://example.com").toURL());
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(mockLocator);
            underTest.startNode(mockNode2);
            underTest.endNode(IGraphmlLocator2);
            underTest.startEdge(mockEdge);
            underTest.endEdge();
            underTest.endGraph(mockLocator);
            underTest.endDocument();
        }

        @Test
        void shouldWorkAsIntendedWithGraph() throws IOException {
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.endGraph(mockLocator);
            underTest.endDocument();
        }

        @Test
        void shouldWorkAsIntendedWithNode() throws IOException {
            when(mockNode.id()).thenReturn(NODE_ID_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            mockNode.setId(NODE_ID_1);
            underTest.startNode(mockNode);
            underTest.endNode(mockLocator);
            underTest.endGraph(mockLocator);
            underTest.endDocument();
        }

    }

    @DisplayName("Graphmlwriter test should not  pass successfully.")
    @Nested
    class notPassTest {


        static class ParameterData {

            List<String> previousOrders;
            String exceptionThrowerMethod;
            ValidatingGraphMlWriter.CurrentElement offendingElement;
            ValidatingGraphMlWriter.CurrentElement lastStartedElement;

            Class<?> exceptionClass;

            public ParameterData(List<String> previousOrders, String exceptionThrowerMethod, CurrentElement offendingElement, CurrentElement lastStartedElement, Class<?> exceptionClass) {
                this.previousOrders = previousOrders;
                this.exceptionThrowerMethod = exceptionThrowerMethod;
                this.offendingElement = offendingElement;
                this.lastStartedElement = lastStartedElement;
                this.exceptionClass = exceptionClass;
            }

            public ParameterData(String[] previousOrders, String exceptionThrowerMethod, ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, Class<?> exceptionClass) {
                this(List.of(previousOrders), exceptionThrowerMethod, offendingElement, lastStartedElement, exceptionClass);
            }

        }

        private static Stream<ParameterData> parameterDataList() {
            List<ParameterData> parameterDataList = new ArrayList<>();
            parameterDataList.add(new ParameterData(List.of(), "key", CurrentElement.EMPTY, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "data", CurrentElement.EMPTY, CurrentElement.DATA, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "startGraph", CurrentElement.EMPTY, CurrentElement.GRAPH, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "startNode", CurrentElement.EMPTY, CurrentElement.NODE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "startEdge", CurrentElement.EMPTY, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "startHyperEdge", CurrentElement.EMPTY, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "startPort", CurrentElement.EMPTY, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of(), "endGraph", CurrentElement.GRAPH, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of(), "endNode", CurrentElement.NODE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of(), "endEdge", CurrentElement.EDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            //10
            parameterDataList.add(new ParameterData(List.of(), "endEdge", CurrentElement.EDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of(), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of(), "endPort", CurrentElement.PORT, CurrentElement.EMPTY, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "startNode", CurrentElement.GRAPHML, CurrentElement.NODE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "startEdge", CurrentElement.GRAPHML, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "startHyperEdge", CurrentElement.GRAPHML, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "startPort", CurrentElement.GRAPHML, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "endGraph", CurrentElement.GRAPH, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "endNode", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "endEdge", CurrentElement.EDGE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            //20
            parameterDataList.add(new ParameterData(List.of("startDocument"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument"), "endPort", CurrentElement.PORT, CurrentElement.GRAPHML, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "startDocument", CurrentElement.GRAPH, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "key", CurrentElement.GRAPH, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "endDocument", CurrentElement.GRAPHML, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "endNode", CurrentElement.NODE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "endEdge", CurrentElement.EDGE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph"), "endPort", CurrentElement.PORT, CurrentElement.GRAPH, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "startDocument", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            //30
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "key", CurrentElement.NODE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "startDocument", CurrentElement.NODE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "endDocument", CurrentElement.GRAPHML, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "endGraph", CurrentElement.GRAPH, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "endEdge", CurrentElement.EDGE, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode"), "endPort", CurrentElement.PORT, CurrentElement.NODE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startEdge"), "key", CurrentElement.EDGE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startEdge"), "endDocument", CurrentElement.GRAPHML, CurrentElement.EDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startEdge"), "endGraph", CurrentElement.GRAPH, CurrentElement.EDGE, GraphmlWriterEndException.class));
            //40
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startEdge"), "endNode", CurrentElement.NODE, CurrentElement.EDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "endDocument", CurrentElement.GRAPHML, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "startDocument", CurrentElement.HYPEREDGE, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "startEdge", CurrentElement.HYPEREDGE, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "startPort", CurrentElement.HYPEREDGE, CurrentElement.PORT, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "key", CurrentElement.HYPEREDGE, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "endGraph", CurrentElement.GRAPH, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "endNode", CurrentElement.NODE, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "endEdge", CurrentElement.EDGE, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startHyperEdge"), "endPort", CurrentElement.PORT, CurrentElement.HYPEREDGE, GraphmlWriterEndException.class));
            //50
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "endDocument", CurrentElement.GRAPHML, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "startDocument", CurrentElement.PORT, CurrentElement.GRAPHML, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "startEdge", CurrentElement.PORT, CurrentElement.EDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "startHyperEdge", CurrentElement.PORT, CurrentElement.HYPEREDGE, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "key", CurrentElement.PORT, CurrentElement.KEY, GraphmlWriterStartException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "endGraph", CurrentElement.GRAPH, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "endNode", CurrentElement.NODE, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "endEdge", CurrentElement.EDGE, CurrentElement.PORT, GraphmlWriterEndException.class));
            parameterDataList.add(new ParameterData(List.of("startDocument", "startGraph", "startNode", "startPort"), "endHyperEdge", CurrentElement.HYPEREDGE, CurrentElement.PORT, GraphmlWriterEndException.class));

            return parameterDataList.stream();
        }

        @ParameterizedTest
        @MethodSource("parameterDataList")
        void shouldThrowException(ParameterData parameterData) throws Exception {
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

        @Test
        void shouldThrowExceptionWhenHyperEdgeRefersToNonExistingNode() throws IOException {
            when(mockNode.id()).thenReturn(NODE_ID_3);
            when(mockHyperEdge.endpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));
            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(mockLocator);
            underTest.startHyperEdge(mockHyperEdge);
            underTest.endHyperEdge();
            underTest.endGraph(mockLocator);

            IllegalStateException illegalStateException = assertThrowsExactly(IllegalStateException.class,
                    () -> underTest.endDocument());
            assertAll("",//
                    () -> assertEquals("2 nodes used in the graph without reference.", illegalStateException.getMessage()));

        }

        @Test
        void shouldThrowExceptionWhenNodeIdIsNotUnique() throws IOException {
            GraphmlNode mockNode2 = mock(GraphmlNode.class);

            when(mockNode.id()).thenReturn(NODE_ID_1);
            when(mockNode2.id()).thenReturn(NODE_ID_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.endNode(mockLocator);

            assertThrows(IllegalStateException.class, () -> underTest.startNode(mockNode2));
        }

        @Test
        void shouldThrowExceptionWhenPortNameIsNotUnique() throws IOException {

            when(mockNode.id()).thenReturn(NODE_ID_1);
            when(mockPort.name()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);
            underTest.startNode(mockNode);
            underTest.startPort(mockPort);
            Exception exception = assertThrows(Exception.class,
                    () -> underTest.startPort(mockPort));

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class, exception),//
                    () -> assertEquals("Port must have a unique Name, but name is used several times: 'port_name_1'.", exception.getMessage()));

        }

        @Test
        void shouldThrowExceptionWhenPortRefersNonExisting() throws IOException {
            when(mockNode.id()).thenReturn(NODE_ID_1);
            // edge: [node1:123] --> [node1:456]
            when(mockEdge.source()).thenReturn(NODE_ID_1);
            when(mockEdge.target()).thenReturn(NODE_ID_1);
            when(mockEdge.sourcePort()).thenReturn("123");
            when(mockEdge.targetPort()).thenReturn("456");
            when(mockPort.name()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);

            underTest.startNode(mockNode);
            // define unused port "port_name_1"
            underTest.startPort(mockPort);
            underTest.endPort();
            underTest.endNode(null);
            underTest.startEdge(mockEdge);
            underTest.endEdge();
            underTest.endGraph(null);


            Exception exception = assertThrows(Exception.class,
                    () -> underTest.endDocument());

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class, exception),//
                    () -> assertEquals("2 ports used in the graph without reference.", exception.getMessage()));
        }

        @Test
        void shouldThrowExceptionWhenPortRefersNonExistingInHyperEdge() throws IOException {
            when(mockNode.id()).thenReturn(NODE_ID_1);
            IGraphmlEndpoint ENDPOINT_TEST_PORT = IGraphmlEndpoint.builder().node(NODE_ID_1).port("123").build();
            when(mockHyperEdge.endpoints()).thenReturn(List.of(ENDPOINT_TEST_PORT, ENDPOINT_TEST_PORT));
            when(mockPort.name()).thenReturn(PORT_NAME_1);

            underTest.startDocument(mockDocument);
            underTest.startGraph(mockGraph);

            underTest.startNode(mockNode);
            underTest.startPort(mockPort);
            underTest.endPort();
            underTest.endNode(null);
            underTest.startHyperEdge(mockHyperEdge);
            underTest.endHyperEdge();
            underTest.endGraph(null);


            Exception exception = assertThrows(Exception.class,
                    () -> underTest.endDocument());

            assertAll("",//
                    () -> assertInstanceOf(IllegalStateException.class, exception),//
                    () -> assertEquals("1 ports used in the graph without reference.", exception.getMessage()));

        }

        private void execute(String methodName) throws IOException {
            switch (methodName) {
                case "key" -> underTest.key(mockKey);
                case "data" -> underTest.data(mockData);
                case "startDocument" -> underTest.startDocument(mockDocument);
                case "startGraph" -> underTest.startGraph(mockGraph);
                case "startNode" -> {
                    when(mockNode.id()).thenReturn(NODE_ID_1);
                    underTest.startNode(mockNode);
                }
                case "startEdge" -> {
                    when(mockEdge.source()).thenReturn(NODE_ID_1);
                    when(mockEdge.target()).thenReturn(NODE_ID_2);
                    underTest.startEdge(mockEdge);
                }
                case "startHyperEdge" -> {
                    when(mockHyperEdge.endpoints()).thenReturn(List.of(ENDPOINT_1, ENDPOINT_2));
                    underTest.startHyperEdge(mockHyperEdge);
                }
                case "startPort" -> {
                    when(mockPort.name()).thenReturn(PORT_NAME_1);
                    underTest.startPort(mockPort);
                }
                case "endDocument" -> underTest.endDocument();
                case "endGraph" -> underTest.endGraph(null);
                case "endNode" -> underTest.endNode(null);
                case "endEdge" -> underTest.endEdge();
                case "endHyperEdge" -> underTest.endHyperEdge();
                case "endPort" -> underTest.endPort();
                default -> throw new IllegalArgumentException(String.format("Method <%s> not supported.", methodName));
            }
        }

    }

    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String NODE_ID_3 = "node3";
    public static final String EDGE_ID_1 = "edge1";
    public static final String PORT_NAME_1 = "port_name_1";
    public static final IGraphmlEndpoint ENDPOINT_1 = IGraphmlEndpoint.builder().node(NODE_ID_1).build();
    public static final IGraphmlEndpoint ENDPOINT_2 = IGraphmlEndpoint.builder().node(NODE_ID_2).build();
    private AutoCloseable closeable;
    private ValidatingGraphMlWriter underTest;
    @Mock
    private IGraphmlLocator mockLocator;
    @Mock
    private GraphmlGraph mockGraph;
    @Mock
    private IGraphmlDocument mockDocument;
    @Mock
    private GraphmlNode mockNode;
    @Mock
    private GraphmlHyperEdge mockHyperEdge;
    @Mock
    private GraphmlEdge mockEdge;
    @Mock
    private IGraphmlKey mockKey;
    @Mock
    private GraphmlData mockData;
    @Mock
    private IGraphmlPort mockPort;

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() throws MalformedURLException {
        closeable = MockitoAnnotations.openMocks(this);
        underTest = new ValidatingGraphMlWriter();
        when(mockLocator.xlinkHref()).thenReturn(URI.create("http://example.com").toURL());
    }

}
