package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RDF (Resource Description Framework)", ".rdf", ".ttl", ".nt", ".n3");
    private static final Logger log = LoggerFactory.getLogger(RdfReader.class);
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
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
            String lang = detectRdfLanguage(singleInputSource.name());
            model.read(is, null, lang);

            // Convert RDF to CJ
            // FIXME must come from parameters
            String baseUri = "https://example.com/";
            convertRdfToCj(model, cjStream, baseUri);
        } catch (Exception e) {
            throw new IOException("Error reading RDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void convertRdfToCj(Model model, ICjStream cjStream, String baseUri) {
        CjDocumentElement cjDoc = new CjDocumentElement();
        RdfModel2CjDoc.rdfModel2cjDoc(model, cjDoc, baseUri);
        ICjWriter cjWriter = new CjWriter2CjStream(cjStream);
        cjDoc.fire(cjWriter);
    }

    private String detectRdfLanguage(String path) {
        if (path.endsWith(".ttl")) return "TURTLE";
        if (path.endsWith(".nt")) return "N-TRIPLES";
        if (path.endsWith(".n3")) return "N3";
        if (path.endsWith(".jsonld")) return "JSON-LD";
        return "RDF/XML"; // default
    }

}
