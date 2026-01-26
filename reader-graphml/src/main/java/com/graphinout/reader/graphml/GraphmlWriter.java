package com.graphinout.reader.graphml;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;

import java.io.IOException;

public class GraphmlWriter implements GioWriter {

    @Override
    public GioFileFormat fileFormat() {
        return GraphmlReader.FORMAT;
    }

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

    private void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        IGraphmlWriter gw = new Graphml2XmlWriter(xmlWriter);
        CjDocument2Graphml.writeToGraphml(cjDoc, gw);
        String graphml = xmlWriter.resultString();
        outputSink.write(graphml);
    }

}
