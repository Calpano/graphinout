package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
class GraphmlReaderTest {

    @BeforeEach
    void setUp() {
    }


    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws IOException {
        log.info("Start To pars file [{}]",filePath);
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);
        OutputSink outputSink = new InMemoryOutputSink();

        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);
    }

    @Test
    void read() throws IOException {
        Path inputSource = Paths.get("src","test","resources","graphin","graphml","samples","graph1_test.graphml");
        URI resourceUri =inputSource.toUri();
        String content =IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);

        OutputSink outputSink = new OutputSink() {
            @Override
            public OutputStream outputStream() throws IOException {
                Path outputSource = Paths.get("src","test","resources","graphin","graphml","samples","graph1_out.graphml");

                return new FileOutputStream(outputSource.toFile());
            }

        };

        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);
     //   outputSink.readAllData().forEach(s -> log.info(s));


    }
    @Test
    void read_AWS_Analytics_graphml() throws IOException {
        Path inputSource = Paths.get("src","test","resources","graphin","graphml","aws","AWS - Analytics.graphml");
        URI resourceUri =inputSource.toUri();
        String content =IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);

        OutputSink outputSink = new OutputSink() {
            @Override
            public OutputStream outputStream() throws IOException {

                Path outputSource = Paths.get("src","test","resources","graphin","graphml","aws","AWS - Analytics_out.graphml");

                return new FileOutputStream(outputSource.toFile());
            }

        };

        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);
        //   outputSink.readAllData().forEach(s -> log.info(s));


    }

    private static Stream<String> getAllGraphmlFiles() {
        return new ClassGraph().scan()
                .getAllResources()
                .stream()
                .map(Resource::getPath)
                .filter(path -> path.endsWith(".graphml"));
    }
}