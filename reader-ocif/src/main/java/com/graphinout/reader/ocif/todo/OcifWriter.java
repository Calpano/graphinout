package com.graphinout.reader.ocif.todo;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.reader.ocif.OcifReader;

import java.io.IOException;

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
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    public void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        String ocif = new OcifOutput(cjDoc).toOcif();
        outputSink.write(ocif);
    }

    @Override
    public GioFileFormat fileFormat() {
        return OcifReader.FORMAT;
    }

}
