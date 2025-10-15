package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.Xml2AppendableWriter;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderContentErrorTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderContentErrorTest.class);

    private final List<String> invalidFiles = new ArrayList<>();


    @Test
    void elementsGraphmlDoesNotAllowCharacter_invalid_root() throws Exception {
        Path inputSource = Paths.get("../base/src/test/resources/xml/graphml/synthetic/invalidgraphml-root.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
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
            assertEquals("Unexpected characters '\n" + "    Hello\n" + "' [No open element to add characters to.]", contentErrorsResult.get(1).getMessage());
            assertEquals("4:1", contentErrorsResult.get(1).getLocation().toString());

            assertEquals(ContentError.ErrorLevel.Warn, contentErrorsResult.get(2).getLevel());
            assertEquals("The Element </myroot> not acceptable tag for Graphml.", contentErrorsResult.get(2).getMessage());
            assertEquals("4:10", contentErrorsResult.get(2).getLocation().toString());
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String displayName, Resource gmlResource) throws Exception {
        if (TestFileUtil.isInvalid(gmlResource, "graphml", "xml")) {
            return;
        }

        if (gmlResource.getPath().endsWith("schema-1--INVALIDgraphml.graphml"))
            // FIXME #115
            return;

        log.info("Start to parse file [{}]", gmlResource.getPath());

        if (invalidFiles.stream().anyMatch(s -> gmlResource.getPath().endsWith(s))) {
            log.info("This file is known as invalid.");
            return;
        }
        try (SingleInputSource singleInputSource = inputSource(gmlResource)) {
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
        invalidFiles.add("xml/graphml/synthetic/root--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/haitimap2--INVALIDgraphml.graphml");
        invalidFiles.add("xml/graphml/greek2--INVALIDgraphml.graphml");
    }


}
