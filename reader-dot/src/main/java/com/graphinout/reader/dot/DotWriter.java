package com.graphinout.reader.dot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.text.ITextWriter;

public class DotWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> writeCjDocument(cjDoc, outputSink));
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    @Override
    public GioFileFormat fileFormat() {
        return DotReader.FORMAT;
    }

    @Override
    public void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) {
        ITextWriter textWriter = ITextWriter.onOutputSink(outputSink);
        CjDocument2Dot.toDotSyntax(cjDoc, textWriter);
    }

}
