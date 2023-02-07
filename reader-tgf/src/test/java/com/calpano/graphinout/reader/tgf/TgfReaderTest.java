package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.XMLFileWriter;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class TgfReaderTest {

    public static final String EMPTY_FILE = "";

    @ParameterizedTest
    // TODO add @MethodSource and use all *.tgf files in via https://github.com/classgraph/classgraph/wiki/Code-examples#finding-and-reading-resource-files
    // requires classgraph as test dependency
    @ValueSource(strings = {"/example.tgf", "/example2.tgf"})
    void shouldWorkAsIntended(String filePath) throws IOException {
        String content = IOUtils.resourceToString(filePath, StandardCharsets.UTF_8);
        InputSource inputSource = InputSource.of(filePath, content);
        OutputSink outputSink = OutputSink.createMock();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XMLFileWriter(outputSink)));
        tgfReader.read(inputSource, gioWriter);
    }

    @Test
    public void shouldCallErrorConsumerWhenTGFIsNotValid() throws IOException {
        Consumer<GioReader.ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        InputSource inputSource = mock(InputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(inputSource.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));
        tgfReader.read(inputSource, gioWriter);

        verify(errorConsumer).accept(any(GioReader.ContentError.class));
    }
}