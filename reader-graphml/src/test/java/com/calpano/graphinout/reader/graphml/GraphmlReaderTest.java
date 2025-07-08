package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class GraphmlReaderTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderTest.class);

    private static Stream<String> getAllGraphmlFiles() {
        return ReaderTests.getAllTestResourceFilePaths()
                .filter(path -> path.endsWith(".graphml"));
    }

    @BeforeEach
    void setUp() {
    }

    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws Exception {
        log.info("Start To pars file [{}]", filePath);
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            graphmlReader.read(singleInputSource, gioWriter);
        }
    }

    @Test
    void read() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "samples", "graph1_test.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            graphmlReader.read(singleInputSource, gioWriter);
        }
    }

    @Test
    void read_AWS_Analytics_graphml() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "aws", "AWS - Analytics.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            graphmlReader.read(singleInputSource, gioWriter);
        }

    }
}
