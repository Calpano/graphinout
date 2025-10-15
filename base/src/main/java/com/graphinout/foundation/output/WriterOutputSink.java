package com.graphinout.foundation.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class WriterOutputSink implements OutputSink {

    private final OutputStream outputStream;
    private final OutputStreamWriter writer;

    public WriterOutputSink(OutputStream outputStream, Charset encoding) {
        this.outputStream = outputStream;
        this.writer = new OutputStreamWriter(outputStream, encoding);
    }

    @Override
    public void close() throws IOException {writer.close();}

    public void flush() throws IOException {writer.flush();}

    @Override
    public OutputStream outputStream() throws IOException {
        return outputStream;
    }

    public void write(String s) throws IOException {
        writer.write(s);
    }

}
