package com.calpano.graphinout.reader.tgf;

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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class TgfReaderTest {
    public static final String EMPTY_FILE = "";
    public static final String NODES_ONLY = "1 First node\n2 Second node";
    public static final String EDGES_ONLY = "#\n1 2 Edge between first and second\n2 3 Edge between second and third\"";
    public static final String THREE_NODES_TWO_EDGES = "1 First node\n2 Second node\n3 Third node\n#\n1 2\n2 3";
    public static final String THREE_NODES_TWO_EDGES_WITH_LABEL = "1 First node\n2 Second node\n3 Third node\n#\n1 2 Label\n2 3";
    private AutoCloseable closeable;
    private TgfReader underTest;
    @Mock
    private GioWriter mockGioWriter;
    @Mock
    private SingleInputSource mockInputSrc;
    @Mock
    private Consumer<ContentError> mockErrorConsumer;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.underTest = new TgfReader();
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);

        underTest.read(singleInputSource, mockGioWriter);
    }

    @Test
    void shouldCallErrorConsumerWhenTGFIsNotValid() throws IOException {
        when(mockInputSrc.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        verifyNoInteractions(mockErrorConsumer);
    }

    @Test
    void shouldReturnErrorWhenTgfFileHasOnlyEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EDGES_ONLY.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(inputStream);

        underTest.errorHandler(mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        verify(mockErrorConsumer).accept(any(ContentError.class));
    }

    @ParameterizedTest
    @MethodSource("tgfSources")
    void shouldWorkAsIntendedAndCallGioWriter(String source) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
        when(mockInputSrc.inputStream()).thenReturn(byteArrayInputStream);

        underTest.errorHandler(TgfReaderTest.this.mockErrorConsumer);
        underTest.read(mockInputSrc, mockGioWriter);

        verify(mockGioWriter).startDocument(any(GioDocument.class));
        verify(mockGioWriter).startGraph(any(GioGraph.class));
        Mockito.verify(mockGioWriter, Mockito.times(3)).startNode(any(GioNode.class));
        Mockito.verify(mockGioWriter, Mockito.times(3)).endNode(Mockito.any());
        Mockito.verify(mockGioWriter, Mockito.times(2)).startEdge(any(GioEdge.class));
        Mockito.verify(mockGioWriter, Mockito.times(2)).endEdge();
        verify(mockGioWriter).endGraph(Mockito.any());
        verify(mockGioWriter).endDocument();
    }

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(path -> path.endsWith(".tgf"));
    }

    private static Stream<String> tgfSources() {
        return Stream.of(THREE_NODES_TWO_EDGES_WITH_LABEL, THREE_NODES_TWO_EDGES);
    }
}