package com.graphinout.reader.tgf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

public class TgfWriter implements GioWriter {

    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void setContentErrorHandler(Consumer<ContentError> contentErrorHandler) {
        this.errorHandler = contentErrorHandler;
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                writeCjDocument(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return TgfReader.FORMAT;
    }

    @Override
    public void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        String tgf = new TgfOutput(cjDoc, errorHandler).toTgf();
        outputSink.write(tgf);
    }

}
