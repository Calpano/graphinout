package com.calpano.graphinout.base.input;


import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
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
        return new SingleInputSource() {
            private final ByteArrayInputStream tmp = new ByteArrayInputStream(bytes);

            @Override
            public void close() throws Exception {
                final Logger log = getLogger("SingleInputSource.class");
                log.debug("Closed inputStream <{}> type <{}>.", name(), inputStream().getClass().getName());
                tmp.close();
            }

            @Override
            public Optional<Charset> encoding() {
                return Optional.empty();
            }

            @Override
            public Optional<String> inputFormat() {
                return Optional.empty();
            }

            @Override
            public InputStream inputStream() throws IOException {
                return tmp;
            }

            @Override
            public String name() {
                return "InMemory";
            }

        };
    }

    /**
     * can be called repeatedly, but users need to close {@link InputStream after each usage
     */
    Optional<Charset> encoding();

    Optional<String> inputFormat();

    InputStream inputStream() throws IOException;

    default boolean isSingle() {
        return true;
    }

    /**
     * e.g. the canonical filename
     */
    @Override
    String name();

}
