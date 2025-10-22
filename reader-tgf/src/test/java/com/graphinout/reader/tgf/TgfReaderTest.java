package com.graphinout.reader.tgf;

import com.graphinout.base.CjStream2GioWriter;
import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TgfReaderTest {

    public static final String EMPTY_FILE = "";
    public static final String NODES_ONLY = "1 First node\n2 Second node";
    public static final String EDGES_ONLY = "#\n1 2 Edge between first and second\n2 3 Edge between second and third\"";
    public static final String THREE_NODES_TWO_EDGES_WITH_LABEL = "1 First node\n2 Second node\n3 Third node\n#\n1 2 Label\n2 3";
    private AutoCloseable closeable;
    private TgfReader underTest;
    @Mock
    private GioWriter mockGioWriter;
    @Mock
    private SingleInputSource mockInputSrc;
    @Mock
    private Consumer<ContentError> mockErrorConsumer;

    private static Stream<TestFileProvider.TestResource> tgfResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".tgf"));
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new TgfReader();
    }

    @Test
    void shouldCreateNodesWhenTgfFileHasOnlyEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EDGES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, new CjStream2GioWriter(mockGioWriter));

        // Verify interactions counts with adapter semantics
        Mockito.verify(mockGioWriter, times(1)).startDocument(any(GioDocument.class));
        Mockito.verify(mockGioWriter, times(1)).startGraph(any(GioGraph.class));
        Mockito.verify(mockGioWriter, times(3)).startNode(any(GioNode.class));
        Mockito.verify(mockGioWriter, times(6)).endNode(Mockito.any());
        Mockito.verify(mockGioWriter, times(2)).startEdge(any(GioEdge.class));
        // 2 edge labels
        Mockito.verify(mockGioWriter, times(2)).data(any(GioData.class));
        Mockito.verify(mockGioWriter, times(4)).endEdge();
        Mockito.verify(mockGioWriter, times(1)).endGraph(Mockito.any());
        Mockito.verify(mockGioWriter, times(1)).endDocument();

        verifyNoMoreInteractions(mockGioWriter);
    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenTGFIsEmpty() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, new CjStream2GioWriter(mockGioWriter));

        // Only endDocument should be called via adapter on empty input
        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).endDocument();
        verifyNoMoreInteractions(mockGioWriter);
        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, new CjStream2GioWriter(mockGioWriter));

        // Verify counts, order not enforced due to adapter semantics
        Mockito.verify(mockGioWriter, times(1)).startDocument(any(GioDocument.class));
        Mockito.verify(mockGioWriter, times(1)).startGraph(any(GioGraph.class));
        Mockito.verify(mockGioWriter, times(2)).startNode(any(GioNode.class));
        Mockito.verify(mockGioWriter, times(4)).endNode(Mockito.any());

        verifyNoInteractions(mockErrorConsumer);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("tgfResources")
    void shouldWorkAsIntended(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        underTest.read(singleInputSource, new CjStream2GioWriter(mockGioWriter));

        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldWorkAsIntendedAndCallGioWriter() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(THREE_NODES_TWO_EDGES_WITH_LABEL.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(byteArrayInputStream);

        underTest.errorHandler(TgfReaderTest.this.mockErrorConsumer);
        underTest.read(mockInputSrc, new CjStream2GioWriter(mockGioWriter));

        // Verify counts with adapter semantics
        Mockito.verify(mockGioWriter, times(1)).startDocument(any(GioDocument.class));
        Mockito.verify(mockGioWriter, times(1)).startGraph(any(GioGraph.class));
        Mockito.verify(mockGioWriter, times(3)).startNode(any(GioNode.class));
        Mockito.verify(mockGioWriter, times(4)).data(any(GioData.class));
        Mockito.verify(mockGioWriter, times(6)).endNode(Mockito.any());
        Mockito.verify(mockGioWriter, times(2)).startEdge(any(GioEdge.class));
        Mockito.verify(mockGioWriter, times(4)).endEdge();
        Mockito.verify(mockGioWriter, times(1)).endGraph(Mockito.any());
        Mockito.verify(mockGioWriter, times(1)).endDocument();

        verifyNoMoreInteractions(mockGioWriter);
    }

    @Test
    void testProvider() {
        assertThat(TestFileProvider.getAllTestResources().toList()).isNotEmpty();
        assertThat(tgfResources().toList()).isNotEmpty();
    }

}
