package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.LoggingGioWriter;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class GraphmlReaderTest2 extends AbstractReaderTest {

    private static final Logger log = getLogger(GraphmlReaderTest2.class);

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new GraphmlReader());
    }

    @Test
    void test() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "graphin/graphml/synthetic/graphml-key-data.graphml.xml";
        List<ContentError> expectedErrors = Collections.emptyList();
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
        GioWriter gioWriter = new LoggingGioWriter();
        List<ContentError> contentErrors = new ArrayList<>();
        gioReader.errorHandler(contentErrors::add);
        gioReader.read(inputSource, gioWriter);
        log.info("Recorded content errors: " + contentErrors);
    }

    @Test
    void testWithOneResource() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "graphin/graphml/synthetic/graphml-key-data.graphml.xml";
        List<ContentError> expectedErrors = Collections.emptyList();
        ReaderTests.testReadResourceToGraph(gioReader, resourcePath, expectedErrors);
    }

}