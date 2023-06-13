package com.calpano.graphinout.base.input;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

class FileSingleInputSourceTest {

    static class FileSingleInputSourceSpy extends FileSingleInputSource {
        boolean isClosed = false;

        public FileSingleInputSourceSpy(File file) {
            super(file);
            isClosed = false;
        }

        @Override
        public void close() throws IOException {
            super.close();
            isClosed = true;
        }
    }

    private final String fileName = UUID.randomUUID() + "-test.txt";

    @Test
    void close() throws Exception {
        FileSingleInputSourceSpy fileSingleInputSourceSpy2;
        try (FileSingleInputSourceSpy fileSingleInputSourceSpy = new FileSingleInputSourceSpy(new File(fileName))) {
            fileSingleInputSourceSpy2 = fileSingleInputSourceSpy;
            Assertions.assertFalse(fileSingleInputSourceSpy2.isClosed);
        }
        Assertions.assertTrue(fileSingleInputSourceSpy2.isClosed);
    }

    @BeforeEach
    void setUp() throws IOException {
        new File(fileName).createNewFile();
    }

    @AfterEach
    void tearDown() {
        new File(fileName).deleteOnExit();
    }
}
