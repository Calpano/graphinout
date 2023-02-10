package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.SimpleXmlWriter;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TgfReaderTest {

    public static final String EMPTY_FILE = "";

    @Test
    void shouldWorkAsIntended() throws IOException {
        String resourceName = "/example.tgf";
        String content = IOUtils.resourceToString(resourceName, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(resourceName, content);
        OutputSink outputSink = OutputSink.createMock();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(outputSink)));
        tgfReader.read(singleInputSource, gioWriter);
    }

    @Test
    void shouldWorkAsIntendedWithAnotherFile() throws IOException {
        String resourceName = "/example2.tgf";
        String content = IOUtils.resourceToString(resourceName, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(resourceName, content);
        OutputSink outputSink = OutputSink.createMock();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(outputSink)));
        tgfReader.read(singleInputSource, gioWriter);
    }
    
    @Test
    public void shouldCallErrorConsumerWhenTGFIsNotValid() throws IOException {
        Consumer<GioReader.ContentError> errorConsumer = mock(Consumer.class);
        TgfReader tgfReader = new TgfReader();
        tgfReader.errorHandler(errorConsumer);
        SingleInputSource singleInputSource = mock(SingleInputSource.class);
        GioWriter gioWriter = mock(GioWriter.class);

        when(singleInputSource.inputStream()).thenReturn(new ByteArrayInputStream(EMPTY_FILE.getBytes()));
        tgfReader.read(singleInputSource, gioWriter);

        verify(errorConsumer).accept(any(GioReader.ContentError.class));
    }
}