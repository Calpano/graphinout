package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.XMLFileWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class DotTextReaderTest {

    // TODO run on all available test files and check if parser crashes; later also validate GraphML?
    @ParameterizedTest
    @ValueSource(strings = {"/example.dot", "/example2.dot", "/example3.dot", "/example4.dot", "/example5.dot", "/example6.dot",
            "/example7.dot", "/example8.dot", "/example9.dot", "/example10.dot", "/example11.dot", "/example12.dot",})
    void test() throws IOException {
        String resourceName = "/example.dot";
        String content = IOUtils.resourceToString(resourceName, StandardCharsets.UTF_8);
        InputSource inputSource = InputSource.of(resourceName, content);
        OutputSink outputSink = OutputSink.createMock();

        DotTextReader dotTextReader = new DotTextReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XMLFileWriter(outputSink)));
        dotTextReader.read(inputSource, gioWriter);
    }

}