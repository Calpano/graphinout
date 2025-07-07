package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.Location;
import com.calpano.graphinout.base.writer.LoggingGioWriter;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
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
                return Arrays.asList(new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "\n" + "\n" + "  \n" + "\n" + "========================================================' [Element 'graphml' does not allow characters.]", new Location(33, 57)));
            case "graphin/graphml/samples/haitimap2.graphml":
                return Arrays.asList(new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "\n" + "\n" + "  \n" + "\n" + "========================================================' [Element 'graphml' does not allow characters.]", new Location(25, 57)));
            case "graphin/graphml/synthetic/invalid-root.graphml":
                return Arrays.asList( //
                        new ContentError(ContentError.ErrorLevel.Warn, "The Element <myroot> not acceptable tag for Graphml.", new Location(2, 9)), //
                        new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "    Hello\n" + "' [No open element to add characters to.]", new Location(4, 1)), //
                        new ContentError(ContentError.ErrorLevel.Warn, "The Element </myroot> not acceptable tag for Graphml.", new Location(4, 10)) //
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

    /** This test helps to pin-point issues with a specific resource */
    @Test
    @Disabled("intended for local use to debug a specific resource")
    void testWithOneResource() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "graphin/graphml/aws/AWS - Analytics.graphml";
        List<ContentError> expectedErrors = expectedErrors(resourcePath);
        testReadResourceToGraph(gioReader, resourcePath, expectedErrors);
    }

}
