package com.calpano.graphinout.base.output;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOutputSinkTest {

    static class InMemoryOutputSinkSpy extends InMemoryOutputSink {
        boolean isClosed = false;

        @Override
        public void close() throws Exception {
            super.close();
            this.isClosed = true;
        }

    }

    @Test
    void writeTest() throws IOException {
        Random r = new Random();
        InMemoryOutputSink outputSink = new InMemoryOutputSink();

        outputSink.outputStream().write("this is for test ".getBytes());
        assertEquals("this is for test ", outputSink.readAllData().get(0));
        outputSink.outputStream().write("this is for test 2 ".getBytes());
        assertEquals("this is for test this is for test 2 ", outputSink.readAllData().get(0));
        outputSink.outputStream().write("\nthis is for test 3 ".getBytes());
        assertEquals("this is for test this is for test 2 \nthis is for test 3 ", outputSink.readAllData().get(0));
    }

    @Test
    void close() throws Exception {
        InMemoryOutputSinkSpy outputSink2;
        try (InMemoryOutputSinkSpy outputSink = new InMemoryOutputSinkSpy()) {
            outputSink2 = outputSink;
            assertFalse(outputSink2.isClosed);
        }
        assertTrue(outputSink2.isClosed);
    }
}
