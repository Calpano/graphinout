package com.calpano.graphinout.base.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputSourceTest {


    static class InputSourceSpy implements InputSource {
        boolean isClosed = false;

        public InputSourceSpy() {
            this.isClosed = false;
        }

        @Override
        public void close() throws Exception {
            this.isClosed = true;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public String name() {
            return null;
        }
    }

    @Test
    void close() throws Exception {
        InputSourceSpy inputSourceSpy2;
        try (InputSourceSpy inputSourceSpy = new InputSourceSpy()) {
            inputSourceSpy2 = inputSourceSpy;
            assertFalse(inputSourceSpy2.isClosed);
        }
        assertTrue(inputSourceSpy2.isClosed);
    }
}
