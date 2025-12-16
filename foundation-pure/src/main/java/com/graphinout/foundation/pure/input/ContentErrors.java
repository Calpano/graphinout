package com.graphinout.foundation.pure.input;

import com.graphinout.foundation.pure.log.Logger;

import java.util.function.Consumer;

public class ContentErrors {

    public static Consumer<ContentError> NOOP = createNoop();

    public static Consumer<ContentError> createNoop() {
        return contentError -> {};
    }


    public static Consumer<ContentError> createOnLog(Logger log) {
        return contentError -> {
            switch (contentError.level) {
                case Info:
                    log.info(contentError.message + " @" + contentError.location);
                case Warn:
                    log.warn(contentError.message + " @" + contentError.location);
                case Error:
                    log.error(contentError.message + " @" + contentError.location);
                default:
                    log.error("[" + contentError.level + "]" + contentError.message + " @" + contentError.location);
            }
        };
    }

}
