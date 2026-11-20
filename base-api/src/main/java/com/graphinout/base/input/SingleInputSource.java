package com.graphinout.base.input;


import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A single, named {@link InputSource} backed by one stream or string, with an optional character encoding.
 */
public interface SingleInputSource extends InputSource {

    static SingleInputSource of(String name, String content) {
        return new SingleInputSourceOfString(name, content);
    }

    static SingleInputSource of(byte[] bytes) {
        return new ByteArrayInputSource("InMemory", bytes);
    }

    /**
     * Auto-closing an inputsource closes all streams handed out via #inputstream()
     */
    @Override
    void close() throws IOException;

    /**
     * can be called repeatedly, but users need to close {@link InputStream after each usage
     */
    Optional<Charset> encoding();

    /**
     * Unchecked variant of {@link #readContentAsUtf8String()}.
     *
     * @throws UncheckedIOException if reading fails
     */
    default String getContentAsUtf8String() {
        try {
            return readContentAsUtf8String();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Optional<String> inputFormat();

    /**
     * @return a new {@link InputStream} on the underlying data
     */
    InputStream inputStream() throws IOException;

    /**
     * Reads the whole input as UTF-8 and closes the stream it opened. Malformed bytes are replaced with U+FFFD; a
     * BOM is not stripped.
     */
    default String readContentAsUtf8String() throws IOException {
        try (InputStream is = inputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    default boolean isSingle() {
        return true;
    }

    /**
     * e.g. the canonical filename
     */
    @Override
    String name();

}
