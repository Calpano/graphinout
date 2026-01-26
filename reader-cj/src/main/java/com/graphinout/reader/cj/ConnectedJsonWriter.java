package com.graphinout.reader.cj;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.base.output.OutputSink;

import java.io.IOException;

public class ConnectedJsonWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        boolean sort = true;
        // collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                write(cjDoc, outputSink, sort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, sort);
    }

    @Override
    public GioFileFormat fileFormat() {
        return ConnectedJsonReader.FORMAT;
    }

    private void write(ICjDocument cjDoc, OutputSink outputSink, boolean sort) throws IOException {
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        cjDoc.fire(cj2JsonWriter, sort);
        String json = json2StringWriter.jsonString();
        outputSink.write(json);
    }

}
