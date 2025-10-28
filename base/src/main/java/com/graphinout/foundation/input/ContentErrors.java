package com.graphinout.foundation.input;

import com.graphinout.base.reader.InMemoryErrorHandler;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class ContentErrors {

    private static final Logger log = getLogger(ContentErrors.class);

    public static Consumer<ContentError> SIMPLE_LOGGING = (contentError) -> {
        String msg = buildMessage(contentError);
        switch (contentError.level) {
            case Error -> log.error(msg);
            case Warn -> log.warn(msg);
        }
    };

    private static String buildMessage(ContentError contentError) {
        StringBuilder b = new StringBuilder();
        b.append(contentError.message);
        contentError.location().ifPresent(loc -> b.append("@").append(loc.line()).append(":").append(loc.col()));
        return b.toString();
    }

    public static InMemoryErrorHandler createInMemory() {
        return new InMemoryErrorHandler();
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

    public static boolean hasErrors(List<ContentError> contentErrors) {
        return contentErrors.stream().anyMatch(ce -> ce.level == ContentError.ErrorLevel.Error);
    }

}
