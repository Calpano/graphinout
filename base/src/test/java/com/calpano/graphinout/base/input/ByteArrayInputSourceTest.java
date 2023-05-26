package com.calpano.graphinout.base.input;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteArrayInputSourceTest {

    static class ByteArrayInputSourceSpy extends ByteArrayInputSource {
        boolean isClosed = false;

        public ByteArrayInputSourceSpy(String name, byte[] bytes) {
            super(name, bytes);
            this.isClosed = false;
        }

        @Override
        public void close() throws Exception {
            super.close();
            isClosed = true;
        }
    }

    @Test
    void close() throws Exception {
        String testString = "test string";
        ByteArrayInputSourceSpy byteArrayInputSourceSpy2;
        try (ByteArrayInputSourceSpy byteArrayInputSourceSpy = new ByteArrayInputSourceSpy(testString, testString.getBytes())) {
            byteArrayInputSourceSpy2 = byteArrayInputSourceSpy;
            Assertions.assertFalse(byteArrayInputSourceSpy2.isClosed);
        }
        Assertions.assertTrue(byteArrayInputSourceSpy2.isClosed);
    }
}
