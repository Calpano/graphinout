package com.calpano.graphinout.base.output;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputSinkTest {
    @Test
    void close() throws Exception {
        OutputSinkSpy OutputSinkSpy2;
        try (OutputSinkSpy OutputSinkSpy = new OutputSinkSpy()) {
            OutputSinkSpy2 = OutputSinkSpy;
            assertFalse(OutputSinkSpy2.isClosed);
        }
        assertTrue(OutputSinkSpy2.isClosed);
    }

    class OutputSinkSpy implements OutputSink {
        boolean isClosed = false;


        @Override
        public void close() throws Exception {
            this.isClosed = true;
        }

        @Override
        public OutputStream outputStream() throws IOException {
            return null;
        }
    }
}