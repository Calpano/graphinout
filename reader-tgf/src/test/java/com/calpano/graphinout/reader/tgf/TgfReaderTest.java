package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import com.calpano.graphinout.base.reader.ContentError;
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
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        tgfReader.read(singleInputSource, gioWriter);
    }

    @Test
    void shouldWorkAsIntendedWithAnotherFile() throws IOException {
        String resourceName = "/example2.tgf";
        String content = IOUtils.resourceToString(resourceName, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(resourceName, content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = ReaderTests.createWriter(outputSink, true,true,true);
        tgfReader.read(singleInputSource, gioWriter);
    }
    
    @Test
    void shouldCallErrorConsumerWhenTGFIsNotValid() throws IOException {
        Consumer<ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        SingleInputSource singleInputSource = mock(SingleInputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(singleInputSource.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));
        tgfReader.read(singleInputSource, gioWriter);

        verifyNoInteractions(errorConsumer);
    }

    @Test
    void shouldNotReturnErrorWhenTgfFileHasNoEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(NODES_ONLY.getBytes(StandardCharsets.UTF_8));
        Consumer<ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        SingleInputSource inputSource = mock(SingleInputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(inputStream);
        tgfReader.read(inputSource, gioWriter);

        verifyNoInteractions(errorConsumer);
    }

    @Test
    void shouldReturnErrorWhenTgfFileHasOnlyEdges() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EDGES_ONLY.getBytes(StandardCharsets.UTF_8));
        Consumer<ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        SingleInputSource inputSource = mock(SingleInputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(inputStream);
        tgfReader.read(inputSource, gioWriter);

        verify(errorConsumer).accept(any(ContentError.class));
    }

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan()
                .getAllResources()
                .stream()
                .map(Resource::getPath)
                .filter(path -> path.endsWith(".tgf"));
    }
}