package com.graphinout.reader.ocif07;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.ocif07.cj.CjDoc2OcifDoc;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OcifWriter implements GioWriter {

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
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return OcifReader.FORMAT;
    }

    private void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        // TODO where to send these errors
        List<ContentError> contentErrors = new ArrayList<>();
        OcifDocument ocifDocument = CjDoc2OcifDoc.toOcifDocument(cjDoc, contentErrors::add);
        String ocifJson = OcifDoc2Json.toJsonString(ocifDocument);
        outputSink.write(ocifJson);
    }

}
