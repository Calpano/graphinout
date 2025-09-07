package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

    private List<String> invalidFiles = new ArrayList<>();

    private static Stream<String> getAllGraphmlFiles() {
        return ReaderTests.getAllTestResourceFilePaths()
                .filter(path -> path.endsWith(".graphml"));
    }

    @Test
    void elementsGraphmlDoesNotAllowCharacter_invalid_root() throws Exception {
        Path inputSource = Paths.get("../base/src/test/resources/xml/graphml/synthetic/invalid-root.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)
        ) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
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

    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws Exception {

        // FIXME #115
        if (filePath.endsWith("graphin/graphml/invalidSchema/invalid-schema-1.graphml"))
            return;

        log.info("Start to parse file [{}]", filePath);
        URL resourceUrl = ClassLoader.getSystemResource(filePath);

        if (invalidFiles.stream().anyMatch(s -> resourceUrl.getPath().endsWith(s))) {
            log.info("This file is known as invalid.");
            return;
        }
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
            graphmlReader.read(singleInputSource, gioWriter);
            assertEquals(0, contentErrors.stream().count());
        }
    }

    @BeforeEach
    void setUp() {
        // /Users/maxvolkel/_data_/_git/GitHubOld/graphinout/base/target/test-classes/xml/graphml/synthetic/invalid-root.graphml
        invalidFiles.add("graphml/synthetic/invalid-root.graphml");
        invalidFiles.add("graphml/samples/invalid-haitimap2.graphml");
        invalidFiles.add("graphml/samples/invalid-greek2.graphml");
    }


}
