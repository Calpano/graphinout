package com.graphinout.reader.jgrapht.dot;

import com.graphinout.base.cj.BaseCjOutput;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

class DotTextReaderTest {

    public static final String EXAMPLE_DOT_PATH = "/text/dot/example.dot";
    public static final String SIMPLE_DOT = "/text/dot/synthetics/simple/simple.dot";
    public static final String SIMPLE_2_DOT = "/text/dot/synthetics/simple/simple2.dot";
    public static final String SIMPLE_3_DOT = "/text/dot/synthetics/simple/simple3.dot";
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
    private static final Logger log = getLogger(DotTextReaderTest.class);
    private final BaseCjOutput baseCjOutput = new BaseCjOutput();
    private AutoCloseable closeable;
    private DotReader underTest;
    @Mock
    private ICjStream mockCjStream;
    @Mock
    private Consumer<ContentError> mockErrorConsumer;

    public static Stream<TestFileProvider.TestResource> dotResources() {
        return TestFileProvider.getAllTestResources() //
                .filter(res -> res.resource().getPath().endsWith(".dot"));
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new DotReader();

        // chunks
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> baseCjOutput.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> baseCjOutput.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> baseCjOutput.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> baseCjOutput.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
        when(mockCjStream.contentErrorHandler()).thenAnswer(inv -> baseCjOutput.contentErrorHandler());
        when(mockCjStream.locator()).thenAnswer(inv -> baseCjOutput.locator());
        // all variants of sendContentError_Error
        when(mockCjStream.sendContentError_Error(any(), any())).thenAnswer(inv -> baseCjOutput.sendContentError_Error(inv.getArgument(0), inv.getArgument(1)));
        when(mockCjStream.sendContentError_Error(any())).thenAnswer(inv -> baseCjOutput.sendContentError_Error(inv.getArgument(0)));
    }

    @ParameterizedTest
    @MethodSource("dotResources")
    void shouldWorkAsIntended(String displayPath, Resource resource) throws IOException {
        if (TestFileUtil.isInvalid(resource, "dot")) {
            log.info("Skipping invalid file " + resource.getURI());
            return;
        }

        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter);
        underTest.read(singleInputSource, cjStream2CjWriter);
        String json = json2StringWriter.jsonString();
        log.info("JSON: "+json);
    }

    @Test
    void shouldWorkAsIntendedWithCallingGioWriter() throws IOException {
        String content = IOUtils.resourceToString(EXAMPLE_DOT_PATH, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(EXAMPLE_DOT_PATH, content);

        underTest.read(inputSource, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        inOrder.verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        // 5 nodes in example.dot
        verify(mockCjStream, times(5)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(5)).nodeEnd();
        // 4 edges
        verify(mockCjStream, times(4)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(4)).edgeEnd();
        inOrder.verify(mockCjStream).graphEnd();
        inOrder.verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_DOT, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 3 nodes and 2 edges
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).nodeEnd();
        verify(mockCjStream, times(2)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(2)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile2() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_2_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_2_DOT, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 2 nodes and 1 edge
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).nodeEnd();
        verify(mockCjStream, times(1)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile3() throws IOException {
        String content = IOUtils.resourceToString(SIMPLE_3_DOT, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(SIMPLE_3_DOT, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 4 nodes and 3 edges
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(4)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(4)).nodeEnd();
        verify(mockCjStream, times(3)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(3)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile4() throws IOException {
        String path = "/text/dot/synthetics/simple/simple4.dot";
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 2 nodes and 1 edge
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).nodeEnd();
        verify(mockCjStream, times(1)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile5() throws IOException {
        String path = "/text/dot/synthetics/simple/simple5.dot";
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 3 nodes and 3 edges (undirected)
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).nodeEnd();
        verify(mockCjStream, times(3)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(3)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    void testSimpleDotFile6() throws IOException {
        String path = "/text/dot/synthetics/simple/simple6.dot";
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, mockCjStream);

        // Expect 2 nodes and 1 edge
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).nodeEnd();
        verify(mockCjStream, times(1)).edgeStart(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).edgeEnd();
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();
    }

    @Test
    @Disabled("not yet possible given the jgrapht API, see #112")
    void testSubgraph() throws IOException {
        String path = "/text/dot/synthetics/simple/simple-subgraph.dot";
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, mockCjStream);
        // no further assertions in disabled test
    }

    @Test
    void testUndirected() throws IOException {
        testRead("/text/dot/synthetics/simple/simple-undirected.dot");
    }

    private void testRead(String path) throws IOException {
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, mockCjStream);
    }

}
