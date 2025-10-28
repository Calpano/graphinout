package com.graphinout.base;

import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.base.writer.ValidatingCjWriter;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * One of the most central interfaces in Graphinout. Defines the contract for reading graph data from a file.
 */
public interface GioReader {

    Logger _log = getLogger(GioReader.class);

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
        setContentErrorHandler(eh);
        try {
            ValidatingCjWriter validatingCjWriter = new ValidatingCjWriter();
            CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(validatingCjWriter);
            read(singleInputSource, cjStream2CjWriter);
        } catch (Throwable t) {
            _log.warn("Invalid input in {}", singleInputSource.name(), t);
            eh.accept(new ContentError(ContentError.ErrorLevel.Error, t.getMessage(), null));
        }
        return valid.get();
    }

    void read(InputSource inputSource, ICjStream cjStream) throws IOException;

    default @Nullable ICjDocument readToCjDocument(InputSource inputSource) throws IOException {
        CjWriter2CjDocumentWriter cjStream2CjDocumentWriter = new CjWriter2CjDocumentWriter();
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cjStream2CjDocumentWriter);
        read(inputSource, cjStream2CjWriter);
        return cjStream2CjDocumentWriter.resultDoc();
    }

    /**
     * Set the error handler to the reader. Reader will use it to report errors while parsing. IOExceptions remain
     * normal IOExceptions.
     */
    void setContentErrorHandler(Consumer<ContentError> errorHandler);

}
