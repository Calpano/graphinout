package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.SimpleXmlWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class TgfReaderTest {

    @Test
    void test() throws IOException {
        String resourceName = "/example.tgf";
        String content = IOUtils.resourceToString(resourceName, StandardCharsets.UTF_8);
        InputSource inputSource = InputSource.of(resourceName,content);
        OutputSink outputSink = OutputSink.createMock();

        TgfReader tgfReader = new TgfReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(outputSink)));
        tgfReader.read(inputSource, gioWriter);
    }

}