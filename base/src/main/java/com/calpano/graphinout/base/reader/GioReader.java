package com.calpano.graphinout.base.reader;

import com.calpano.graphinout.base.gio.DelegatingGioWriter;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.gio.ValidatingGioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter;
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
            if (error.level == ContentError.ErrorLevel.Error || error.level == ContentError.ErrorLevel.Warn) {
                valid.set(false);
            }
        };
        errorHandler(eh);
        try {
            GioWriter writer = new DelegatingGioWriter(new ValidatingGioWriter(), new GioWriterImpl(new ValidatingGraphMlWriter()));
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
