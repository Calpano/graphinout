package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.LoggingGioWriter;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.calpano.graphinout.base.ReaderTests.testReadResourceToGraph;
import static org.slf4j.LoggerFactory.getLogger;

class GraphmlReaderTest2 extends AbstractReaderTest {

    private static final Logger log = getLogger(GraphmlReaderTest2.class);

    protected List<ContentError> expectedErrors(String resourceName) {
        switch (resourceName) {
            case "graphin/graphml/samples/greek2.graphml":
                return Arrays.asList(
                        new ContentError(ContentError.ErrorLevel.Warn,"Unexpected characters '\n" +
                                "\n" +
                                "\n" +
                                "  \n" +
                                "\n" +
                                "========================================================' [Element 'graphml' does not allow characters.]",new ContentError.Location(33,57))
                );
        }
        return Collections.emptyList();
    }

    protected List<GioReader> readersToTest() {
        return List.of(new GraphmlReader());
    }

    /** test with a LoggingReader to see GioWriter calls 1:1 */
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
    @Disabled("See issue #96 : This test was done in GraphmlReaderContentErrorTest's class")
    void testWithOneResource() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "graphin/graphml/samples/greek2.graphml";
        List<ContentError> expectedErrors = expectedErrors(resourcePath);
        testReadResourceToGraph(gioReader, resourcePath, expectedErrors);
    }

}
