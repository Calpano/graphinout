package com.graphinout.reader.adjlist;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdjListReaderTest {

    public static final String EMPTY_FILE = "";
    public static final String NODES_ONLY = "aaa\nbbb";
    public static final String SMALL = "aaa bbb ccc\nddd aaa";
    private final CjFactory cjFactory = new CjFactory();
    private AutoCloseable closeable;
    private AdjListReader underTest;
    @Mock private ICjStream mockCjStream;
    private List<ContentError> capturedErrors;
    private Consumer<ContentError> errorConsumer;

    private static Stream<TestFileProvider.TestResource> adjListResourcePaths() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".adjlist"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new AdjListReader();
        this.capturedErrors = new ArrayList<>();
        this.errorConsumer = capturedErrors::add;

        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> cjFactory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> cjFactory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> cjFactory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> cjFactory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);

    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenTGFIsEmpty() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-empty", EMPTY_FILE);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        verifyNoInteractions(mockCjStream);
        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-nodes-only", NODES_ONLY);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        inOrder.verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        inOrder.verify(mockCjStream).nodeStart(any(ICjNodeChunk.class));
        inOrder.verify(mockCjStream).nodeEnd();
        inOrder.verify(mockCjStream).nodeStart(any(ICjNodeChunk.class));
        inOrder.verify(mockCjStream).nodeEnd();

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

    @Test
    void shouldWork() throws IOException {
        SingleInputSource inputSource = SingleInputSource.of("test-small", SMALL);

        underTest.setContentErrorHandler(errorConsumer);
        underTest.read(inputSource, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        inOrder.verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        // aaa
        inOrder.verify(mockCjStream).nodeStart(cjFactory.createNodeChunkWithId("aaa"));
        inOrder.verify(mockCjStream).nodeEnd();
        // bbb
        inOrder.verify(mockCjStream).nodeStart(cjFactory.createNodeChunkWithId("bbb"));
        inOrder.verify(mockCjStream).nodeEnd();
        // aaa -> bbb
        inOrder.verify(mockCjStream).edgeStart(any(ICjEdgeChunk.class));
        inOrder.verify(mockCjStream).edgeEnd();
        // ccc
        inOrder.verify(mockCjStream).nodeStart(cjFactory.createNodeChunkWithId("ccc"));
        inOrder.verify(mockCjStream).nodeEnd();
        // aaa -> ccc
        inOrder.verify(mockCjStream).edgeStart(any(ICjEdgeChunk.class));
        inOrder.verify(mockCjStream).edgeEnd();
        // ddd
        inOrder.verify(mockCjStream).nodeStart(cjFactory.createNodeChunkWithId("ddd"));
        inOrder.verify(mockCjStream).nodeEnd();
        // ddd -> aaa
        inOrder.verify(mockCjStream).edgeStart(any(ICjEdgeChunk.class));
        inOrder.verify(mockCjStream).edgeEnd();
        inOrder.verify(mockCjStream).graphEnd();
        inOrder.verify(mockCjStream).documentEnd();

        //verifyNoMoreInteractions(mockCjStream);
    }

    @ParameterizedTest
    @MethodSource("adjListResourcePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockCjStream);

        // Verify no errors were captured
        assert capturedErrors.isEmpty();
    }

}
