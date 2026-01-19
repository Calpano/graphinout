package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class RdfWriter implements GioWriter {

    private static final Logger log = LoggerFactory.getLogger(RdfWriter.class);

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // Collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                write(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    @Override
    public GioFileFormat fileFormat() {
        return RdfReader.FORMAT;
    }

    public void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        CjDoc2RdfModel.cjDoc2Model(cjDoc, model);

        // add namespace declarations to model
        model.setNsPrefix("base", nonNullOrDefault(cjDoc.baseUri(), "#"));
        model.setNsPrefix("cj", RdfCj.CjInRdf.VOC);
        model.setNsPrefix("rdf", RDF.uri);
        model.setNsPrefix("rdfs", RDFS.uri);

        // Write RDF as Turtle (more readable than RDF/XML)
        StringWriter stringWriter = new StringWriter();
        model.write(stringWriter, "TURTLE");
        outputSink.write(stringWriter.toString());
    }


}
