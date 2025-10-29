package com.graphinout.reader.tgf;

import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TgfReaderTest {

    public static final String EMPTY_FILE = "";
    public static final String NODES_ONLY = """
            1 First node
            2 Second node""";
    public static final String EDGES_ONLY = """
            #
            1 2 Edge between first and second
            2 3 Edge between second and third\"""";
    public static final String THREE_NODES_TWO_EDGES_WITH_LABEL = """
            1 First node
            2 Second node
            3 Third node
            #
            1 2 Label
            2 3""";
    private AutoCloseable closeable;
    private TgfReader underTest;
    @Mock private ICjStream mockCjStream;
    @Mock private SingleInputSource mockInputSrc;
    @Mock private Consumer<ContentError> mockErrorConsumer;

    private static Stream<TestFileProvider.TestResource> tgfResources() {
        return TestFileProvider.getAllTestResources()
                .filter(res -> res.resource().getPath().endsWith(".tgf"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new TgfReader();

        // The reader requests chunk instances and a JSON factory from the stream; stub the mock accordingly
        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldCreateNodesWhenTgfFileHasOnlyEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EDGES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.setContentErrorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        // Verify interactions counts with adapter semantics
        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(1)).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(1)).graphEnd();
        verify(mockCjStream, times(1)).documentEnd();

    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenTGFIsEmpty() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));

        underTest.setContentErrorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        InOrder inOrder = Mockito.inOrder(mockCjStream);
        inOrder.verify(mockCjStream).createDocumentChunk();
        inOrder.verify(mockCjStream).document(any(ICjDocumentChunk.class));
        verifyNoMoreInteractions(mockCjStream);
        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.setContentErrorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        // Verify counts, order not enforced due to adapter semantics
        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(1)).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(2)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(2)).nodeEnd();

        verifyNoInteractions(mockErrorConsumer);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("tgfResources")
    void shouldWorkAsIntended(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        underTest.read(singleInputSource, mockCjStream);

        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldWorkAsIntendedAndCallCjStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(THREE_NODES_TWO_EDGES_WITH_LABEL.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(byteArrayInputStream);

        underTest.setContentErrorHandler(TgfReaderTest.this.mockErrorConsumer);
        underTest.read(mockInputSrc, mockCjStream);

        verify(mockCjStream).createDocumentChunk();
        verify(mockCjStream).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream).createGraphChunk();
        verify(mockCjStream).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(3)).createNodeChunk();
        verify(mockCjStream, times(4)).jsonFactory();
        verify(mockCjStream, times(3)).nodeStart(any(ICjNodeChunk.class));
        verify(mockCjStream, times(3)).nodeEnd();
        verify(mockCjStream, times(2)).createEdgeChunk();
        verify(mockCjStream, times(2)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream).graphEnd();
        verify(mockCjStream).documentEnd();

        verifyNoMoreInteractions(mockCjStream);
    }

    @Test
    void testProvider() {
        assertThat(TestFileProvider.getAllTestResources().toList()).isNotEmpty();
        assertThat(tgfResources().toList()).isNotEmpty();
    }

}
