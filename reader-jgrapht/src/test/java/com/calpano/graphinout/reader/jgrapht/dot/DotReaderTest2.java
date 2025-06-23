package com.calpano.graphinout.reader.jgrapht.dot;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.validation.GraphmlValidator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class DotReaderTest2 extends AbstractReaderTest {
    private static final Logger log = getLogger(DotReaderTest2.class);

    protected List<ContentError> expectedErrors(String resourceName) {
        if (resourceName.endsWith("Call_Graph.gv")) {
            return Arrays.asList(new ContentError(ContentError.ErrorLevel.Error, "at line 35:22 extraneous input ',' expecting {'{', '}', GRAPH, NODE, EDGE, SUBGRAPH, NUMBER, STRING, ID, HTML_STRING}", null));
        }
        if (resourceName.endsWith("libinput-stack-xorg.gv")) {
            return Arrays.asList(new ContentError(ContentError.ErrorLevel.Error, "at line 6:15 extraneous input ';' expecting {']', NUMBER, STRING, ID, HTML_STRING}", null));
        }
        if (resourceName.endsWith("oberon.gv")) {
            return Arrays.asList(new ContentError(ContentError.ErrorLevel.Error, "at line 32:6 extraneous input ',' expecting {'{', '}', GRAPH, NODE, EDGE, SUBGRAPH, NUMBER, STRING, ID, HTML_STRING}", null));
        }
        return Collections.emptyList();
    }

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new DotReader());
    }

    @Test
    void test() {
        // stream incoming test resource as XML to the logger
        GioReader gioReader = new DotReader();
        ReaderTests.forEachReadableResource(gioReader, resourcePath -> {
            Writer w = null;
            InMemoryOutputSink outputSink = OutputSink.createInMemory();
            try {
                ReaderTests.readResourceToSink(gioReader, resourcePath, outputSink, true, true, true);
                String s = new String(outputSink.getByteBuffer().toByteArray(), StandardCharsets.UTF_8);
                GraphmlValidator.isValidGraphml(SingleInputSource.of("parsed", s));
                log.info("Read:\n" + s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testOneResource() throws IOException {
        String path = "dot/synthetics/synthetic2.dot";
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        ReaderTests.readResourceToSink(new DotReader(), path, outputSink, true, true, true);
        String s = new String(outputSink.getByteBuffer().toByteArray(), StandardCharsets.UTF_8);
        GraphmlValidator.isValidGraphml(SingleInputSource.of("parsed", s));
        log.info("Read:\n" + s);
    }

}
