package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.output.OutputSink;

import java.io.IOException;
import java.util.function.Consumer;

public class GexfWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                write(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    private void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {

    }

    @Override
    public GioFileFormat fileFormat() {
        return GexfReader.FORMAT;
    }

}
