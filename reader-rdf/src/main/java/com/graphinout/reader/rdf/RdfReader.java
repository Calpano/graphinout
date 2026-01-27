package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class RdfReader implements GioReader, GioWriter {

    public static final String BASE_URI = "baseUri";
    private static final Logger log = LoggerFactory.getLogger(RdfReader.class);
    public final GioFileFormat format; // = new GioFileFormat(FORMAT_ID, "RDF (Resource Description Framework)", ".rdf", ".rdf.xml", ".ttl", ".nt", ".n3");
    private final RdfFormats.RdfSyntax rdfSyntax;
    private @Nullable Consumer<ContentError> errorHandler;

    public RdfReader(GioFileFormat format, RdfFormats.RdfSyntax rdfSyntax) {
        this.format = format;
        this.rdfSyntax = rdfSyntax;
    }

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
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return format;
    }

    public RdfFormats.RdfSyntax rdfSyntax() {
        return rdfSyntax;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        try (InputStream is = singleInputSource.inputStream()) {
            Model model = ModelFactory.createDefaultModel();

            // Detect RDF format from file extension
            RdfFormats.RdfSyntax lang = RdfFormats.detectRdfLanguage(singleInputSource.name(), rdfSyntax);
            model.read(is, null, lang.jenaName);

            String baseUri = "";
            if (inputSource.isParameterized()) {
                baseUri = inputSource.asParameterized().getValue(BASE_URI);
            }
            // Convert RDF to CJ
            convertRdfToCj(model, cjStream, baseUri);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(ContentError.error(e.getMessage()));
            }
            throw new IOException("Error reading RDF from '" + inputSource.name() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        Model model = CjDoc2RdfModel.cjDoc2Model(cjDoc);

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

    private void convertRdfToCj(Model model, ICjStream cjStream, String baseUri) {
        CjDocumentElement cjDoc = new CjDocumentElement();
        RdfModel2CjDoc.rdfModel2cjDoc(model, cjDoc, baseUri);
        ICjWriter cjWriter = new CjWriter2CjStream(cjStream);
        cjDoc.fire(cjWriter, false);
    }


}
