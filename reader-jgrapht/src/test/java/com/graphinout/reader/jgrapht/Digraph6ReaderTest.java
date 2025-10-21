package com.graphinout.reader.jgrapht;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.ReaderTests;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.validation.graphml.GraphmlValidator;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.output.InMemoryOutputSink;
import com.graphinout.foundation.output.OutputSink;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class Digraph6ReaderTest extends AbstractReaderTest {

    private static final Logger log = getLogger(Digraph6ReaderTest.class);

    @Override
    protected List<GioReader> readersToTest() {
        return Arrays.asList(new Digraph6Reader());
    }

    @Test
    void test() {
        GioReader gioReader = new Digraph6Reader();
        // stream incoming test resource as XML to the logger
        ReaderTests.forEachReadableResource(gioReader, resourcePath -> {
            InMemoryOutputSink outputSink = OutputSink.createInMemory();
            try {
                ReaderTests.readResourceToSink(gioReader, resourcePath, outputSink);
                String s = outputSink.getBufferAsUtf8String();
                GraphmlValidator.isValidGraphml(SingleInputSource.of("parsed", s));
                log.info("Read:\n" + s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
