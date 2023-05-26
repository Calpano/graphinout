package com.calpano.graphinout.base.input;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

class FileSingleInputSourceTest {

    private String fileName = UUID.randomUUID() + "-" + LocalDateTime.now() + "_test.txt";

    @BeforeEach
    void setUp() throws IOException {
        new File(fileName).createNewFile();
    }

    @AfterEach
    void tearDown() {
        new File(fileName).deleteOnExit();
    }

    @Test
    void close() throws Exception {
        FileSingleInputSourceSpy fileSingleInputSourceSpy2;
        try (FileSingleInputSourceSpy fileSingleInputSourceSpy = new FileSingleInputSourceSpy(new File(fileName))) {
            fileSingleInputSourceSpy2 = fileSingleInputSourceSpy;
            Assertions.assertFalse(fileSingleInputSourceSpy2.isClosed);
        }
        Assertions.assertTrue(fileSingleInputSourceSpy2.isClosed);
    }

    class FileSingleInputSourceSpy extends FileSingleInputSource {
        boolean isClosed = false;

        public FileSingleInputSourceSpy(File file) {
            super(file);
            isClosed = false;
        }

        @Override
        public void close() throws Exception {
            super.close();
            isClosed = true;
        }
    }
}