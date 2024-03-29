package com.calpano.graphinout.base.output;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
public class InMemoryOutputSink implements OutputSink {

    private final ByteArrayOutputStream buffer;

    public InMemoryOutputSink() {
        buffer = new ByteArrayOutputStream();
    }

    public String getBufferAsUtf8String() {
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return getBufferAsUtf8String();
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

    @Override
    public void close() throws Exception {
        log.debug("Closed OutputSink  <InMemoryOutputSink> type <{}>.", buffer.getClass().getName());
        buffer.close();
    }
}
