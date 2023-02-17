package com.calpano.graphinout.base.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class InMemoryOutputSink implements OutputSink {

    private final ByteArrayOutputStream buffer;

    public InMemoryOutputSink() {
        buffer = new ByteArrayOutputStream();
    }

    public String getBufferAsUtf8String() {
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    public ByteArrayOutputStream getByteBuffer() {
        return buffer;
    }

    @Override
    public OutputStream outputStream() throws IOException {
        return buffer;
    }

    public List<String> readAllData() throws IOException {
        return Collections.singletonList(buffer.toString());
    }


}
