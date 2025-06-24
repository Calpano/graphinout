package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

    private List<String> invalidFiles = new ArrayList<>();

    private static Stream<String> getAllGraphmlFiles() {
        return ReaderTests.getAllTestResourceFilePaths()
                .filter(path -> path.endsWith(".graphml"));
    }

    @BeforeEach
    void setUp() {
        invalidFiles.add(Paths.get("target", "test-classes", "graphin", "graphml", "synthetic", "invalid-root.graphml").toUri().getPath());
        invalidFiles.add(Paths.get("target", "test-classes", "graphin", "graphml", "samples", "haitimap2.graphml").toUri().getPath());
        invalidFiles.add(Paths.get("target", "test-classes", "graphin", "graphml", "samples", "greek2.graphml").toUri().getPath());
    }

    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws Exception {

        // FIXME #115
        if(filePath.endsWith("graphin/graphml/invalidSchema/invalidSchema-1.graphml"))
            return;

        log.info("Start To pars file [{}]", filePath);
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        if (invalidFiles.removeIf(s -> resourceUrl.getPath().equals(s))) {
            log.info("This file is known as invalid.");
            return;
        }
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            graphmlReader.read(singleInputSource, gioWriter);
            assertEquals(0, contentErrors.stream().count());
        }
    }


    @Test
    void elementsGraphmlDoesNotAllowCharacter_invalid_root() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic", "invalid-root.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
             OutputSink outputSink = new InMemoryOutputSink()) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
            graphmlReader.read(singleInputSource, gioWriter);
            List<ContentError> contentErrorsResult = contentErrors.stream().toList();
            assertEquals(3, contentErrorsResult.size());
            assertEquals(ContentError.ErrorLevel.Warn, contentErrorsResult.get(0).getLevel());
            assertEquals("The Element <myroot> not acceptable tag for Graphml.", contentErrorsResult.get(0).getMessage());
            assertEquals("2:9", contentErrorsResult.get(0).getLocation().toString());

            assertEquals(ContentError.ErrorLevel.Warn, contentErrorsResult.get(1).getLevel());
            assertEquals("Unexpected characters '\n" +
                    "    Hello\n" +
                    "' [No open element to add characters to.]", contentErrorsResult.get(1).getMessage());
            assertEquals("4:1", contentErrorsResult.get(1).getLocation().toString());

            assertEquals(ContentError.ErrorLevel.Warn, contentErrorsResult.get(2).getLevel());
            assertEquals("The Element </myroot> not acceptable tag for Graphml.", contentErrorsResult.get(2).getMessage());
            assertEquals("4:10", contentErrorsResult.get(2).getLocation().toString());
        }
    }



}
