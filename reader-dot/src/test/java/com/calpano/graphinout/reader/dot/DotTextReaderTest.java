package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.*;
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
import org.mockito.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DotTextReaderTest {
    public static final String EXAMPLE_DOT_PATH = "/example.dot";
    public static final String SIMPLE_DOT = "/synthetics/simple/simple.dot";
    public static final String SIMPLE_2_DOT = "/synthetics/simple/simple2.dot";
    public static final String SIMPLE_3_DOT = "/synthetics/simple/simple3.dot";
    public static final String SIMPLE_4_DOT = "/synthetics/simple/simple4.dot";
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
    public static final String EDGE_ID_A_C = "A-C";
    public static final String EDGE_ID_A_B = "A-B";
    public static final String EDGE_ID_B_C = "B-C";
    public static final String EDGE_ID_C_D = "C-D";
    public static final String COLOR = "color";
    public static final String COLOR_VALUE = "red";
    public static final String COLOR_VALUE_2 = "lightblue";
    public static final String SHAPE = "shape";
    public static final String SHAPE_VALUE = "circle";
    private AutoCloseable closeable;
    private DotTextReader underTest;
    @Mock
    private GioWriter mockGioWriter;
    @Mock
    private Consumer<ContentError> mockErrorConsumer;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new DotTextReader();
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
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

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(path -> path.endsWith(".dot"));
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

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_C).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_A_B).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_A_C).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_C).build()).build());
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
        assertEquals(LABEL, capturedData.get(0).getKey());
        assertEquals(NODE_A, capturedData.get(0).getValue());
        assertEquals(LABEL, capturedData.get(1).getKey());
        assertEquals(NODE_B, capturedData.get(1).getValue());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_A_B).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
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

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_C).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        inOrder.verify(mockGioWriter).startNode(GioNode.builder().id(NODE_ID_D).build());
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        ArgumentCaptor<GioData> dataCaptor = ArgumentCaptor.forClass(GioData.class);
        verify(mockGioWriter, times(3)).data(dataCaptor.capture());

        List<GioData> capturedData = dataCaptor.getAllValues();
        assertEquals(3, capturedData.size());
        assertEquals(LABEL, capturedData.get(0).getKey());
        assertEquals(EDGE_1, capturedData.get(0).getValue());
        assertEquals(LABEL, capturedData.get(1).getKey());
        assertEquals(EDGE_2, capturedData.get(1).getValue());
        assertEquals(LABEL, capturedData.get(2).getKey());
        assertEquals(EDGE_3, capturedData.get(2).getValue());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_A_B).endpoint(GioEndpoint.builder().node("A").build()).endpoint(GioEndpoint.builder().node("B").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_B_C).endpoint(GioEndpoint.builder().node("B").build()).endpoint(GioEndpoint.builder().node("C").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_C_D).endpoint(GioEndpoint.builder().node("C").build()).endpoint(GioEndpoint.builder().node("D").build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

    @Test
    void testSimpleDotFile4() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_4_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_4_DOT, content);
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
        verify(mockGioWriter, times(5)).data(dataCaptor.capture());

        List<GioData> capturedData = dataCaptor.getAllValues();
        assertEquals(5, capturedData.size());

        assertEquals(SHAPE, capturedData.get(0).getKey());
        assertEquals(SHAPE_VALUE, capturedData.get(0).getValue());
        assertEquals(COLOR, capturedData.get(1).getKey());
        assertEquals(COLOR_VALUE_2, capturedData.get(1).getValue());

        assertEquals(LABEL, capturedData.get(2).getKey());
        assertEquals(NODE_A, capturedData.get(2).getValue());
        assertEquals(COLOR, capturedData.get(3).getKey());
        assertEquals(COLOR_VALUE_2, capturedData.get(3).getValue());
        assertEquals(LABEL, capturedData.get(4).getKey());
        assertEquals(NODE_B, capturedData.get(4).getValue());

        inOrder.verify(mockGioWriter).startEdge(GioEdge.builder().id(EDGE_ID_A_B).endpoint(GioEndpoint.builder().node(NODE_ID_A).build()).endpoint(GioEndpoint.builder().node(NODE_ID_B).build()).build());
        inOrder.verify(mockGioWriter).endEdge();

        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();
    }

}