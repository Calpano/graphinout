package com.calpano.graphinout.foundation.output;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class FileOutputSink implements OutputSink {

    private final File file;
    private transient OutputStream out;

    public FileOutputSink(File file) {
        this.file = file;
    }

    @Override
    public void close() throws Exception {
        if (out != null) {
            out.close();
            log.debug("Closed OutputSink  <{}}> type <{}>.", file.getName(), out.getClass().getName());
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        if (out == null) {
            out = new FileOutputStream(file);
        }
        return out;
    }
}
