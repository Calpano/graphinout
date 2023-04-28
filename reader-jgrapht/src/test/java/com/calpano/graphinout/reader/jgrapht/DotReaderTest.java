package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.xml.GraphmlValidator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class DotReaderTest extends AbstractReaderTest {

    private static final Logger log = getLogger(DotReaderTest.class);

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new Graph6Reader());
    }

    @Test
    void testOneResource() throws IOException {
        String path = "dot/synthetics/simple/simple4.dot";
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        ReaderTests.readResourceToSink(new DotReader(), path, outputSink, true, true, true);
        String s = new String(outputSink.getByteBuffer().toByteArray(), StandardCharsets.UTF_8);
        GraphmlValidator.isValidGraphml(SingleInputSource.of("parsed", s));
        log.info("Read:\n" + s);
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

}
