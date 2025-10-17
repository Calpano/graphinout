package com.graphinout.base;

import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjDocumentMutable;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.base.gio.GioWriter;

import java.io.IOException;

/**
 * Buffers all Gio-calls into a {@link ICjDocumentMutable} and writes it at {@link #endDocument()} to the {@link GioWriter}.
 */
public class Gio2CjWriter extends Gio2CjDocumentWriter implements GioWriter {

    private final ICjWriter cjWriter;

    public Gio2CjWriter(ICjWriter cjWriter) {this.cjWriter = cjWriter;}

    @Override
    public void endDocument() throws IOException {
        super.endDocument();

        ICjDocument cjDoc = resultDocument();
        assert cjDoc != null;
        cjDoc.fire(cjWriter);
    }


}
