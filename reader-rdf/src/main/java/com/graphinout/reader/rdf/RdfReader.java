package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;

public class RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RDF (Resource Description Framework)", ".rdf", ".ttl", ".nt", ".n3");
    public static final String BLANK_NODE_PSEUDO_SCHEME = "_:";
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
        convertRdfToCj(model, cjDoc, baseUri);
        ICjWriter cjWriter = new CjWriter2CjStream(cjStream);
        cjDoc.fire(cjWriter);
    }

    private void convertRdfToCj(Model model, ICjDocumentMutable cjDoc, String baseUri) {
        cjDoc.connectedJson(c -> {
            // TODO later
            c.canonical(false);
            c.versionDate(CjConstants.CJ_LATEST_VERSION_DATE);
            c.versionNumber(CjConstants.CJ_LATEST_VERSION_NUMBER);
        });
        // TODO use it to shorten URIs
        cjDoc.baseUri(baseUri);

        cjDoc.addGraph(cjGraph -> {
            // Iterate through all rdfTriples (triples)
            StmtIterator rdfStatements = model.listStatements();
            while (rdfStatements.hasNext()) {
                Statement stmt = rdfStatements.nextStatement();
                // == S
                Resource subject = stmt.getSubject();
                String subjectUri;
                if (subject.isAnon()) {
                    subjectUri = BLANK_NODE_PSEUDO_SCHEME + subject.getId().getLabelString();
                } else {
                    subjectUri = subject.getURI();
                }
                String subjectId = cjDoc.asId(subjectUri);
                // == P
                Property predicate = stmt.getPredicate();
                String predicateId = cjDoc.asId(predicate.getURI());
                // == O
                RDFNode object = stmt.getObject();
                if (object.isResource()) {
                    String objectId;
                    if (object.isURIResource()) {
                        objectId = cjDoc.asId(object.asResource().getURI());
                    } else {
                        assert object.isAnon();
                        objectId = BLANK_NODE_PSEUDO_SCHEME + object.asResource().getId().getLabelString();
                    }
                    ICjEdgeMutable cjEdge = cjGraph.addBiEdge(subjectId, objectId);
                    cjEdge.edgeType(predicateId);
                } else if (object.isLiteral()) {
                    // map to CJ data property
                    Literal literal = object.asLiteral();
                    cjGraph.addNode(cjNode -> {
                        cjNode.id(subjectId);
                        cjNode.dataMutable(cjData -> {
                            cjData.add(pathOf(RdfCj.RdfInCj.rdfData, predicateId), literal.getLexicalForm());
                        });
                    });
                } else {
                    throw new IllegalStateException();
                }
            }
        });
    }

    private String detectRdfLanguage(String path) {
        if (path.endsWith(".ttl")) return "TURTLE";
        if (path.endsWith(".nt")) return "N-TRIPLES";
        if (path.endsWith(".n3")) return "N3";
        if (path.endsWith(".jsonld")) return "JSON-LD";
        return "RDF/XML"; // default
    }

}
