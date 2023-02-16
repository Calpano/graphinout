package com.calpano.graphinout.base.reader;

import org.slf4j.Logger;

import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class ContentErrors {

    private static final Logger log = getLogger(ContentErrors.class);

    private static String buildMessage(ContentError contentError) {
        StringBuilder b = new StringBuilder();
        b.append(contentError.message);
        contentError.location.ifPresent(loc -> b.append("@" + loc.line + ":" + loc.col));
        return b.toString();
    }

    public static InMemoryContentHandler createInMemory() {
        return new InMemoryContentHandler();
    }

    public static Consumer<ContentError> defaultErrorHandler() {
        return (contentError) -> {
            String msg = buildMessage(contentError);
            switch (contentError.level) {
                case Error -> log.error(msg);
                case Warn -> log.warn(msg);
            }
        };
    }
}
