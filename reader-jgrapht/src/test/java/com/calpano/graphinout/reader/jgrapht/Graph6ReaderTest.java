package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.validation.GraphmlValidator;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class Graph6ReaderTest extends AbstractReaderTest {

    private static final Logger log = getLogger(Graph6ReaderTest.class);

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new Graph6Reader());
    }

    @Test
    void test() {
        // stream incoming test resource as XML to the logger
        GioReader gioReader = new Graph6Reader();
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

}
