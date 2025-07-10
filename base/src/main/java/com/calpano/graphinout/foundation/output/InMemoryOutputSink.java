package com.calpano.graphinout.foundation.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class InMemoryOutputSink implements OutputSink {

    private static final Logger log = LoggerFactory.getLogger(InMemoryOutputSink.class);

    private final ByteArrayOutputStream buffer;

    public InMemoryOutputSink() {
        buffer = new ByteArrayOutputStream();
    }

    public InMemoryOutputSink(ByteArrayOutputStream buffer) {
        this.buffer = buffer;
    }

    @Override
    public void close() throws Exception {
        log.debug("Closed OutputSink  <InMemoryOutputSink> type <{}>.", buffer.getClass().getName());
        buffer.close();
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

    @Override
    public String toString() {
        return getBufferAsUtf8String();
    }

}
