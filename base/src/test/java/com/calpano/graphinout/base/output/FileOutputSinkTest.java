package com.calpano.graphinout.base.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileOutputSinkTest {
    static class FileOutputSinkSpy extends FileOutputSink {
        boolean isClosed = false;

        public FileOutputSinkSpy(File file) {
            super(file);
        }


        @Override
        public void close() throws Exception {
            super.close();
            this.isClosed = true;
        }


    }

    private final String fileName = UUID.randomUUID() + "-test.txt";

    @Test
    void close() throws Exception {
        FileOutputSinkSpy fileOutputSinkSpy2;
        try (FileOutputSinkSpy fileOutputSinkSpy = new FileOutputSinkSpy(new File(fileName))) {
            fileOutputSinkSpy2 = fileOutputSinkSpy;
            assertFalse(fileOutputSinkSpy2.isClosed);
        }
        assertTrue(fileOutputSinkSpy2.isClosed);
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
