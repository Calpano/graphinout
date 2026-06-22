package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjConstants;
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
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
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
        // Per-syntax edge-metadata encoding: star-capable syntaxes (Turtle/TriG/N-Triples/N-Quads) use
        // RDF-star triple terms; the rest (RDF/XML, JSON-LD, RDF/JSON, TriX) fall back to classic
        // rdf:Statement reification (plain triples) so edge metadata survives all serialisations.
        Model model = CjDoc2RdfModel.cjDoc2Model(cjDoc, rdfSyntax);

        // add namespace declarations to model
        Map<String, String> context = cjDoc.context();
        String vocab = context != null ? context.get(CjConstants.VOCAB) : null;
        model.setNsPrefix("base", nonNullOrDefault(vocab, "#"));
        model.setNsPrefix("cj", RdfCj.CjInRdf.VOC);
        model.setNsPrefix("rdf", RDF.uri);
        model.setNsPrefix("rdfs", RDFS.uri);
        // Map @context prefix entries back to RDF namespace prefixes
        if (context != null) {
            for (Map.Entry<String, String> entry : context.entrySet()) {
                if (!CjConstants.VOCAB.equals(entry.getKey())) {
                    model.setNsPrefix(entry.getKey(), entry.getValue());
                }
            }
        }

        // Some CJ datasets carry relative node ids (e.g. <node-1>, <tokyo>). The strict line-based writers
        // (N-Triples / N-Quads) reject relative IRIs, and passing a write base would also relativise
        // absolute IRIs such as cj:thisDocument (breaking the read side, which has no base). So resolve any
        // relative IRIs to absolute up front against a base, then serialise WITHOUT a base — absolute IRIs
        // (cj:*, ex:*, …) are left untouched.
        String base = nonNullOrDefault(vocab, RdfCj.CjInRdf.VOC);
        Model absoluteModel = absolutizeRelativeIris(model, base);

        // Serialise using the reader's configured syntax (was hardcoded to Turtle).
        String serialized = RDFWriter.create()
                .source(absoluteModel)
                .format(jenaFormat(rdfSyntax))
                .asString();
        outputSink.write(serialized);
    }

    /**
     * Return a model equivalent to {@code model} but with every relative-IRI resource (a URI with no scheme,
     * e.g. {@code <node-1>}) resolved to an absolute IRI against {@code base}. Absolute IRIs and blank nodes
     * are copied unchanged, and namespace prefixes are preserved. If nothing is relative the original model
     * is returned as-is.
     */
    private static Model absolutizeRelativeIris(Model model, String base) {
        org.apache.jena.graph.Graph in = model.getGraph();
        java.util.List<org.apache.jena.graph.Triple> triples = in.find().toList();
        boolean anyRelative = triples.stream().anyMatch(t -> tripleHasRelative(t));
        if (!anyRelative) return model;

        Model out = ModelFactory.createDefaultModel();
        out.setNsPrefixes(model.getNsPrefixMap());
        for (org.apache.jena.graph.Triple t : triples) {
            out.getGraph().add(org.apache.jena.graph.Triple.create(
                    absolutizeNode(t.getSubject(), base),
                    absolutizeNode(t.getPredicate(), base),
                    absolutizeNode(t.getObject(), base)));
        }
        return out;
    }

    private static boolean tripleHasRelative(org.apache.jena.graph.Triple t) {
        return nodeHasRelative(t.getSubject()) || nodeHasRelative(t.getPredicate()) || nodeHasRelative(t.getObject());
    }

    private static boolean nodeHasRelative(org.apache.jena.graph.Node node) {
        if (node instanceof org.apache.jena.graph.Node_Triple nt) {
            return tripleHasRelative(nt.getTriple());
        }
        return isRelativeUri(node);
    }

    private static boolean isRelativeUri(org.apache.jena.graph.Node node) {
        if (!node.isURI()) return false;
        try {
            return java.net.URI.create(node.getURI()).getScheme() == null;
        } catch (IllegalArgumentException e) {
            return false; // not a parseable URI -> leave untouched
        }
    }

    private static org.apache.jena.graph.Node absolutizeNode(org.apache.jena.graph.Node node, String base) {
        if (node instanceof org.apache.jena.graph.Node_Triple nt) {
            org.apache.jena.graph.Triple t = nt.getTriple();
            return org.apache.jena.graph.NodeFactory.createTripleTerm(
                    absolutizeNode(t.getSubject(), base),
                    absolutizeNode(t.getPredicate(), base),
                    absolutizeNode(t.getObject(), base));
        }
        if (!isRelativeUri(node)) return node;
        return org.apache.jena.graph.NodeFactory.createURI(base + node.getURI());
    }

    /**
     * Map an {@link RdfFormats.RdfSyntax} to the Jena {@link RDFFormat} used to serialise it. The chosen
     * RDF-star-capable Turtle/TriG/N-Triples/N-Quads formats emit triple terms in Jena 5.6; the others
     * (RDF/XML, JSON-LD 1.1, RDF/JSON, TriX) have no triple-term syntax. Namespace prefixes set on the
     * model are honoured by all these writers. Turtle is the default/fallback.
     */
    static RDFFormat jenaFormat(RdfFormats.RdfSyntax syntax) {
        return switch (syntax) {
            case TURTLE -> RDFFormat.TURTLE;
            case N_TRIPLES -> RDFFormat.NTRIPLES;
            case N_QUADS -> RDFFormat.NQUADS;
            case TRIG -> RDFFormat.TRIG;
            case RDF_XML -> RDFFormat.RDFXML;
            case JSON_LD -> RDFFormat.JSONLD;
            case RDF_JSON -> RDFFormat.RDFJSON;
            case TRIX -> RDFFormat.TRIX;
        };
    }

    private void convertRdfToCj(Model model, ICjStream cjStream, String baseUri) {
        CjDocumentElement cjDoc = new CjDocumentElement();
        RdfModel2CjDoc.rdfModel2cjDoc(model, cjDoc, baseUri);
        ICjWriter cjWriter = new CjWriter2CjStream(cjStream);
        cjDoc.fire(cjWriter, false);
    }


}
