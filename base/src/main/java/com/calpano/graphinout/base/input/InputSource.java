package com.calpano.graphinout.base.input;

import com.calpano.graphinout.base.util.GIOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface InputSource {

    static InputSource of(String name, String content) {
        return new InputSource() {
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
                return
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    /** can be called repeatedly, but users need to close {@link InputStream after each usage */
    Optional<Charset> encoding();

    Optional<String> inputFormat();

    InputStream inputStream() throws IOException;

    /**
     * e.g. the canonical filename
     */
    String name();
}
