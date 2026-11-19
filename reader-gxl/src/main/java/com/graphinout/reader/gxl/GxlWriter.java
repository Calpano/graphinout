package com.graphinout.reader.gxl;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrors;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Writes GXL 1.0. See {@link Cj2Gxl} for the mapping and for what GXL cannot represent.
 */
public class GxlWriter implements GioWriter {

    private Consumer<ContentError> errorHandler = ContentErrors.NOOP;

    public static String toGxl(ICjDocument cjDoc, Consumer<ContentError> errorHandler) {
        return new Cj2Gxl(errorHandler).toGxl(cjDoc);
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // GXL output needs the whole graph (e.g. to choose edgemode), so collect into a CJ document first
        CjWriter2CjDocumentWriter documentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                writeCjDocument(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(documentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return GxlReader.FORMAT;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> contentErrorHandler) {
        this.errorHandler = contentErrorHandler;
    }

    @Override
    public void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        outputSink.write(toGxl(cjDoc, errorHandler));
    }

}
