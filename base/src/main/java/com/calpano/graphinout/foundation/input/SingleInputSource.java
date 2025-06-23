package com.calpano.graphinout.foundation.input;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public interface SingleInputSource extends InputSource {

    static SingleInputSource of(String name, String content) {
        return new SingleInputSourceOfString(name, content);
    }

    static SingleInputSource of(byte[] bytes) {
        return new ByteArrayInputSource("InMemory",bytes);
    }

    /**
     * can be called repeatedly, but users need to close {@link InputStream after each usage
     */
    Optional<Charset> encoding();

    Optional<String> inputFormat();

    /**
     * @return a new {@link InputStream} on the underlying data
     */
    InputStream inputStream() throws IOException;

    default boolean isSingle() {
        return true;
    }

    /**
     * e.g. the canonical filename
     */
    @Override
    String name();

    /**
     * Auto-closing an inputsource closes all streams handed out via #inputstream()
     */
    @Override
    void close() throws IOException;

}
