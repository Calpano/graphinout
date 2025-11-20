package com.graphinout.foundation.text;

import com.graphinout.foundation.output.OutputSink;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class TextWriterOnWriter implements ITextWriter, AutoCloseable {

    private final Writer writer;

    public TextWriterOnWriter(OutputSink outputSink) {
        try {
            OutputStream out = outputSink.outputStream();
            this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        writer.flush();
        writer.close();
    }

    @Override
    public void line(String line) {
        try {
            writer.append(line);
            writer.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
