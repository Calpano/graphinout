package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

class Graph6ReaderTest extends AbstractReaderTest {

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
            OutputSink outputSink = OutputSink.createForwarding(w);
            try {
                ReaderTests.readResourceToSink(gioReader, resourcePath, outputSink, true, true, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}