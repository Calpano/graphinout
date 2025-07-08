package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.Gio2GraphmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.validation.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.writer.DelegatingGioWriter;
import com.calpano.graphinout.base.writer.ValidatingGioWriter;
import com.calpano.graphinout.foundation.input.InputSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public interface GioReader {

    Logger log = getLogger(GioReader.class);

    /**
     * Set error handler to the reader. Reader will use it to report errors while parsing. IOExceptions remain normal
     * IOExceptions.
     *
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
     *
     * @param singleInputSource
     * @return
     * @throws IOException
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
            log.warn("Invalid input in "+singleInputSource.name(), t);
            eh.accept(new ContentError(ContentError.ErrorLevel.Error,t.getMessage(), null));
        }
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
