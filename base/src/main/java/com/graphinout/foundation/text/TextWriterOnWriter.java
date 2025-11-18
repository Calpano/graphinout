package com.graphinout.foundation.text;

import java.io.Writer;

public class TextWriterOnWriter implements ITextWriter, AutoCloseable {

    private final Writer writer;

    public TextWriterOnWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }

    @Override
    public void line(String line) {

    }

}
