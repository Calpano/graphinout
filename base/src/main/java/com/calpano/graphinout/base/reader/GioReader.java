package com.calpano.graphinout.base.reader;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface GioReader {

    /**
     * Set error handler to the reader. Reader will use it to report errors while parsing.
     * IOExceptions remain normal IOExceptions.
     * @param errorHandler
     */
    void errorHandler(Consumer<ContentError> errorHandler);

    /**
     * Which file format can this reader read?
     *
     * @return
     */
    GioFileFormat fileFormat();

    /**
     * Inspect input
     * @param singleInputSource
     * @return
     * @throws IOException
     */
    default boolean isValid(SingleInputSource singleInputSource) throws IOException {
        AtomicBoolean valid = new AtomicBoolean(true);
        errorHandler((error) -> {
            if (error.level == ContentError.ErrorLevel.Error) {
                valid.set(false);
            }
        });
        // FIXME dont use null, use a dummy do-nothing-writer
        read(singleInputSource, null);
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
