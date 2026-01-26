package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.reader.graphml.IGraphmlWriter;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.functional.Nullables;

import java.util.function.Consumer;

public class Graphml2CjWriter extends Graphml2CjDocument implements IGraphmlWriter {

    private final ICjWriter cjWriter;

    public Graphml2CjWriter(ICjWriter cjWriter) {
        this.cjWriter = cjWriter;
    }

    @Override
    public void documentEnd() {
        super.documentEnd();
        Nullables.ifPresentAccept(resultDoc(), doc -> doc.fire(cjWriter, false));
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        super.setContentErrorHandler(errorHandler);
        cjWriter.setContentErrorHandler(errorHandler);
    }

}
