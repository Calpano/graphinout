package com.calpano.graphinout.base.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SingleInputSourceOfString implements SingleInputSource {

    final String name;
    final String content;

    public SingleInputSourceOfString(String name, String content) {
        this.name = name;
        this.content = content;
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
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String name() {
        return name;
    }

}
