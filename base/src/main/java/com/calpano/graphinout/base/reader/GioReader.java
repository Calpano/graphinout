package com.calpano.graphinout.base.reader;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.GraphMlWriter;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface GioReader {

    @AllArgsConstructor
    class ContentError {
        public enum ErrorLevel {
            Warn, Error
        }

        public class Location {
            int line;
            int col;
        }
        final ErrorLevel level;
        final String message;
        final Optional<Location> location;
    }

    // inspect

    // validate

    default void errorHandler(Consumer<ContentError> errorConsumer) {
    }

    /**
     * Which file format can this reader read?
     *
     * @return
     */
    GioFileFormat fileFormat();

    default boolean isValid(InputSource inputSource) throws IOException {
        AtomicBoolean valid = new AtomicBoolean(true);
        errorHandler((error) -> {
            if (error.level == ContentError.ErrorLevel.Error) {
                valid.set(false);
            }
        });
        // TODO dont use null, use a dummy do-nothing-writer
        read(inputSource, null);
        return valid.get();
    }

    /**
     * Map all incoming graph structures to the internal GIO model.
     *
     * @param inputSource
     * @param writer
     * @throws IOException
     */
    void read(InputSource inputSource, GioWriter writer) throws IOException;

}
