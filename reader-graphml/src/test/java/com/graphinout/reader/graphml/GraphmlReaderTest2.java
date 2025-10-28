package com.graphinout.reader.graphml;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.GioReader;
import com.graphinout.base.reader.Location;
import com.graphinout.base.writer.LoggingCjStream;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.output.InMemoryOutputSink;
import io.github.classgraph.Resource;
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

import static com.graphinout.base.ReaderTests.testReadResourceToGraph;
import static org.slf4j.LoggerFactory.getLogger;

class GraphmlReaderTest2 extends AbstractReaderTest {

    private static final Logger log = getLogger(GraphmlReaderTest2.class);

    protected List<ContentError> expectedErrors(String resourceName) {
        return switch (resourceName) {
            case "xml/graphml/greek2--INVALIDgraphml.graphml" ->
                    List.of(new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "\n" + "\n" + "  \n" + "\n" + "========================================================' [Element 'graphml' does not allow characters.]", new Location(33, 57)));
            case "xml/graphml/haitimap2--INVALIDgraphml.graphml" ->
                    List.of(new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "\n" + "\n" + "  \n" + "\n" + "========================================================' [Element 'graphml' does not allow characters.]", new Location(25, 57)));
            case "xml/graphml/graphml/synthetic/invalidgraphml-root.graphml" -> Arrays.asList( //
                    new ContentError(ContentError.ErrorLevel.Warn, "The Element <myroot> not acceptable tag for Graphml.", new Location(2, 9)), //
                    new ContentError(ContentError.ErrorLevel.Warn, "Unexpected characters '\n" + "    Hello\n" + "' [No open element to add characters to.]", new Location(4, 1)), //
                    new ContentError(ContentError.ErrorLevel.Warn, "The Element </myroot> not acceptable tag for Graphml.", new Location(4, 10)) //
            );
            default -> Collections.emptyList();
        };
    }

    protected List<GioReader> readersToTest() {
        return List.of(new GraphmlReader());
    }

    /** test with a LoggingReader to see GioWriter calls 1:1 */
    @SuppressWarnings("resource")
    @Test
    void test() throws IOException {
        GioReader gioReader = new GraphmlReader();
        String resourcePath = "xml/graphml/synthetic/graphml-key-data.graphml.xml";
        List<ContentError> expectedErrors = Collections.emptyList();
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
        ICjStream cjStream = new LoggingCjStream();
        List<ContentError> contentErrors = new ArrayList<>();
        gioReader.setContentErrorHandler(contentErrors::add);
        gioReader.read(inputSource, cjStream);
        log.info("Recorded content errors: " + contentErrors);
    }

    /** This test helps to pin-point issues with a specific resource */
    @Test
    @Disabled("intended for local use to debug a specific resource")
    void testWithOneResource() throws IOException {
        GioReader gioReader = new GraphmlReader();
        Resource resource = TestFileUtil.resource("xml/graphml/aws/AWS - Analytics.graphml");
        List<ContentError> expectedErrors = expectedErrors(resource);
        testReadResourceToGraph(gioReader, resource, expectedErrors);
    }

}
