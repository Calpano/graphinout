package com.graphinout.base.gio;

import com.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.validation.graphml.ValidatingGraphMlWriter;
import com.graphinout.base.writer.DelegatingGioWriter;
import com.graphinout.base.writer.ValidatingGioWriter;
import com.graphinout.foundation.input.InputSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * One of the most central interfaces in Graphinout. Defines the contract for reading graph data from a file.
 */
public interface GioReader {

    Logger log = getLogger(GioReader.class);

    /**
     * Set the error handler to the reader. Reader will use it to report errors while parsing. IOExceptions remain
     * normal IOExceptions.
     */
    void errorHandler(Consumer<ContentError> errorHandler);

    /**
     * Which file format can this reader read?
     */
    GioFileFormat fileFormat();

    /**
     * Inspect input
     */
    default boolean isValid(InputSource singleInputSource) throws IOException {
        AtomicBoolean valid = new AtomicBoolean(true);
        Consumer<ContentError> eh = error -> {
            if (error.getLevel() == ContentError.ErrorLevel.Error || error.getLevel() == ContentError.ErrorLevel.Warn) {
                valid.set(false);
            }
        };
        errorHandler(eh);
        try {
            GioWriter writer = new DelegatingGioWriter(new ValidatingGioWriter(), new Gio2GraphmlWriter(new ValidatingGraphMlWriter()));
            read(singleInputSource, writer);
        } catch (Throwable t) {
            log.warn("Invalid input in {}", singleInputSource.name(), t);
            eh.accept(new ContentError(ContentError.ErrorLevel.Error, t.getMessage(), null));
        }
        return valid.get();
    }

    /**
     * Map all incoming graph structures to the internal GIO model.
     */
    void read(InputSource inputSource, GioWriter writer) throws IOException;

}
