package com.calpano.graphinout.base.input;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class SingleInputSourceOfString implements SingleInputSource {

    final String name;
    final String content;

    private final ByteArrayInputStream inputStream;

    public SingleInputSourceOfString(String name, String content) {
        this.name = name;
        this.content = content;
        inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<Charset> encoding() {
        return Optional.of(StandardCharsets.UTF_8);
    }

    @Override
    public Optional<String> inputFormat() {
        return Optional.empty();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void close() throws Exception {
        log.debug("Closed inputStream <{}> type <{}>.", name, inputStream.getClass().getName());
        inputStream.close();
    }
}
