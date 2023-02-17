package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

class DotTextReaderTest {

    // TODO run on all available test files and check if parser crashes; later also validate GraphML?
    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void shouldWorkAsIntended(String filePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);
        OutputSink outputSink = OutputSink.createMock();

        DotTextReader dotTextReader = new DotTextReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        dotTextReader.read(singleInputSource, gioWriter);
    }

    private static Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan()
                .getAllResources()
                .stream()
                .map(Resource::getPath)
                .filter(path -> path.endsWith(".dot"));
    }
}