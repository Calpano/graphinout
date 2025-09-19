package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.TestFileProvider;
import com.calpano.graphinout.foundation.input.SingleInputSource;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
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

    private static Stream<String> tgfResourcePaths() {
        return TestFileProvider.getAllTestResourcePaths().filter(path -> path.endsWith(".tgf"));
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
        underTest.read(mockInputSrc, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

        verifyNoMoreInteractions(mockGioWriter);
    }

    @Test
    void shouldNotCallErrorConsumerAndGioWriterWhenTGFIsEmpty() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        verifyNoInteractions(mockErrorConsumer, mockGioWriter);
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());

        verifyNoInteractions(mockErrorConsumer);
    }

    @ParameterizedTest
    @MethodSource("tgfResourcePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockGioWriter);

        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldWorkAsIntendedAndCallGioWriter() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(THREE_NODES_TWO_EDGES_WITH_LABEL.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(byteArrayInputStream);

        underTest.errorHandler(TgfReaderTest.this.mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        InOrder inOrder = Mockito.inOrder(mockGioWriter);
        inOrder.verify(mockGioWriter).startDocument(any(GioDocument.class));
        inOrder.verify(mockGioWriter).startGraph(any(GioGraph.class));
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).data(any(GioData.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).data(any(GioData.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startNode(any(GioNode.class));
        inOrder.verify(mockGioWriter).data(any(GioData.class));
        inOrder.verify(mockGioWriter).endNode(Mockito.any());
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).startEdge(any(GioEdge.class));
        inOrder.verify(mockGioWriter).endEdge();
        inOrder.verify(mockGioWriter).endGraph(Mockito.any());
        inOrder.verify(mockGioWriter).endDocument();

        verifyNoMoreInteractions(mockGioWriter);
    }

}
