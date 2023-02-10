package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.SimpleXmlWriter;
import com.calpano.graphinout.base.reader.GioReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        InputSource inputSource = InputSource.of(filePath, content);
        OutputSink outputSink = OutputSink.createMock();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(outputSink)));
        tgfReader.read(inputSource, gioWriter);
    }

    @Test
    public void shouldNotReturnErrorWhenTgfFileIsEmpty() throws IOException {
        Consumer<GioReader.ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        InputSource inputSource = mock(InputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));
        tgfReader.read(inputSource, gioWriter);

        verifyNoInteractions(errorConsumer);
    }

    @Test
    public void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        Consumer<GioReader.ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        InputSource inputSource = mock(InputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(inputStream);
        tgfReader.read(inputSource, gioWriter);

        verifyNoInteractions(errorConsumer);
    }

    @Test
    public void shouldReturnErrorWhenTgfFileHasOnlyEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EDGES_ONLY.getBytes(StandardCharsets.UTF_8));
        Consumer<GioReader.ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        InputSource inputSource = mock(InputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(inputStream);
        tgfReader.read(inputSource, gioWriter);

        verify(errorConsumer).accept(any(GioReader.ContentError.class));
    }

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan()
                .getAllResources()
                .stream()
                .map(Resource::getPath)
                .filter(path -> path.endsWith(".tgf"));
    }
}