package com.calpano.graphinout.reader.jgrapht.dot;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioEndpointDirection;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;

class DotTextReaderTest {
    public static final String EXAMPLE_DOT_PATH = "/dot/example.dot";
    public static final String SIMPLE_DOT = "/dot/synthetics/simple/simple.dot";
    public static final String SIMPLE_2_DOT = "/dot/synthetics/simple/simple2.dot";
    public static final String SIMPLE_3_DOT = "/dot/synthetics/simple/simple3.dot";
    public static final String LABEL = "label";
    public static final String NODE_A = "Node A";
    public static final String NODE_B = "Node B";
    public static final String NODE_ID_A = "A";
    public static final String NODE_ID_B = "B";
    public static final String NODE_ID_C = "C";
    public static final String NODE_ID_D = "D";
    public static final String EDGE_1 = "Edge 1";
    public static final String EDGE_2 = "Edge 2";
    public static final String EDGE_3 = "Edge 3";
    public static final String NODE_COLOR = "node-color";
    public static final String NODE_SHAPE = "node-shape";
    public static final String CIRCLE = "circle";
    public static final String COLOR_RED = "red";
    public static final String COLOR_GREEN = "green";
    private static final String NODE_LABEL = "node-label";
    private static final String EDGE_LABEL = "edge-label";
    private static final String EDGE_COLOR = "edge-color";
    private AutoCloseable closeable;
    private DotReader underTest;
    @Mock
    private GioWriter mockGioWriter;
    @Mock
    private Consumer<ContentError> mockErrorConsumer;
    private static final Logger log = getLogger(DotTextReaderTest.class);


    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(path -> path.endsWith(".dot"));
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new DotReader();
    }

    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockGioWriter);
        underTest.errorHandler(mockErrorConsumer);
    }

    @Test
    void shouldWorkAsIntendedWithCallingGioWriter() throws IOException {
        String content = IOUtils.resourceToString(EXAMPLE_DOT_PATH, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(EXAMPLE_DOT_PATH, content);

        underTest.read(inputSource, mockGioWriter);
        underTest.errorHandler(mockErrorConsumer);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    @Test
    void testSimpleDotFile() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_DOT, content);
        GioWriter mockGioWriter = mock(GioWriter.class);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());

        inOrder.verify(mockGioWriter).startGraph(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null) //
                .endpoint(GioEndpoint.builder().node(NODE_ID_A).build()) //
                .endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_C).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_C).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    @Test
    void testSimpleDotFile2() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_2_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_2_DOT, content);
        GioWriter mockGioWriter = mock(GioWriter.class);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());

        inOrder.verify(mockGioWriter).startGraph(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        ArgumentCaptor<GioData> dataCaptor = ArgumentCaptor.forClass(GioData.class);
        verify(mockGioWriter, times(2)).data(dataCaptor.capture());

        List<GioData> capturedData = dataCaptor.getAllValues();
        assertEquals(2, capturedData.size());
        assertEquals(NODE_LABEL, capturedData.get(0).getKey());
        assertEquals(NODE_A, capturedData.get(0).getValue());
        assertEquals(NODE_LABEL, capturedData.get(1).getKey());
        assertEquals(NODE_B, capturedData.get(1).getValue());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    @Test
    void testSimpleDotFile3() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_3_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_3_DOT, content);
        GioWriter mockGioWriter = mock(GioWriter.class);
        underTest.read(inputSource, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());

        inOrder.verify(mockGioWriter).startGraph(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node("A").build()).endpoint(GioEndpoint.builder().node("B").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_C).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node("B").build()).endpoint(GioEndpoint.builder().node("C").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_D).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node("C").build()).endpoint(GioEndpoint.builder().node("D").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

        ArgumentCaptor<GioData> dataCaptor = ArgumentCaptor.forClass(GioData.class);
        verify(mockGioWriter, times(3)).data(dataCaptor.capture());
        List<GioData> capturedData = dataCaptor.getAllValues();
        assertEquals(3, capturedData.size());
        assertEquals(EDGE_LABEL, capturedData.get(0).getKey());
        assertEquals(EDGE_1, capturedData.get(0).getValue());
        assertEquals(EDGE_LABEL, capturedData.get(1).getKey());
        assertEquals(EDGE_2, capturedData.get(1).getValue());
        assertEquals(EDGE_LABEL, capturedData.get(2).getKey());
        assertEquals(EDGE_3, capturedData.get(2).getValue());
    }

    @Test
    void testSimpleDotFile4() throws IOException {
        testRead("/dot/synthetics/simple/simple4.dot");

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());
        inOrder.verify(mockGioWriter).startGraph(Mockito.any());
        // NOTE: paypal parser prepared support for default attributes (in grammar) but
        // didn't implement it in parser.
        // expect: node [shape=circle, color=lightblue];

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        ArgumentCaptor<GioData> dataCaptor = ArgumentCaptor.forClass(GioData.class);
        verify(mockGioWriter, times(8)).data(dataCaptor.capture());

        List<GioData> capturedData = dataCaptor.getAllValues();
        assertEquals(8, capturedData.size());

        assertEquals(Set.of(capturedData.get(0), capturedData.get(1), capturedData.get(2)), //
                Set.of(// inherited and overwritten default attribute
                        GioData.builder().key(NODE_COLOR).value(COLOR_RED).build(),
                        GioData.builder().key(NODE_LABEL).value(NODE_A).build(),
                        // inherited default attribute
                        GioData.builder().key(NODE_SHAPE).value(CIRCLE).build()
                ));

        assertEquals(Set.of(capturedData.get(6), capturedData.get(7)), //
                Set.of(GioData.builder().key(EDGE_COLOR).value(COLOR_GREEN).build(),
                        GioData.builder().key(EDGE_LABEL).value(EDGE_1).build()
                ));

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    @Test
    void testSimpleDotFile6() throws IOException {
        testRead("/dot/synthetics/simple/simple6.dot");

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());
        inOrder.verify(mockGioWriter).startGraph(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).data(GioData.builder().key(NODE_LABEL).value("A").build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    private void testRead(String path) throws IOException {
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        mockGioWriter = mock(GioWriter.class);
        underTest.read(inputSource, mockGioWriter);
    }

    @Test
    void testSimpleDotFile5() throws IOException {
        testRead("/dot/synthetics/simple/simple5.dot");

        MockingDetails details = Mockito.mockingDetails(mockGioWriter);
        log.info(details.printInvocations());

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(Mockito.any());
        inOrder.verify(mockGioWriter).startGraph(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_A).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_B).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_C).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_A).type(GioEndpointDirection.Undirected).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).type(GioEndpointDirection.Undirected).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_B).type(GioEndpointDirection.Undirected).build()).endpoint(GioEndpoint.builder().node(NODE_ID_C).type(GioEndpointDirection.Undirected).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(null).endpoint(GioEndpoint.builder().node(NODE_ID_C).type(GioEndpointDirection.Undirected).build()).endpoint(GioEndpoint.builder().node(NODE_ID_A).type(GioEndpointDirection.Undirected).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

}
