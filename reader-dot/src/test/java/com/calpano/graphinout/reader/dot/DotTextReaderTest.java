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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class DotTextReaderTest {
    public static final String EXAMPLE_DOT_PATH = "/example.dot";
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

        verify(mockGioWriter).startDocument(any(GioDocument.class));
        verify(mockGioWriter).startGraph(any(GioGraph.class));
        Mockito.verify(mockGioWriter, Mockito.times(6)).startNode(any(GioNode.class));
        Mockito.verify(mockGioWriter, Mockito.times(6)).endNode(Mockito.any());
        Mockito.verify(mockGioWriter, Mockito.times(7)).startEdge(any(GioEdge.class));
        Mockito.verify(mockGioWriter, Mockito.times(7)).endEdge();
        verify(mockGioWriter).endGraph(Mockito.any());
        verify(mockGioWriter).endDocument();
    }

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(path -> path.endsWith(".dot"));
    }
}