package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.Nullable;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RdfModels {

    public static int count(Model rdfModel, @Nullable Resource s, @Nullable Property p, @Nullable RDFNode o, int maxCount) {
        int i = 0;
        StmtIterator stmts = rdfModel.listStatements(s, p, o);
        while (stmts.hasNext()) {
            i++;
            if (i >= maxCount) break;
            stmts.next();
        }
        return i;
    }

    public static String normalize(String rdfNQuads) {
        Model rdfModel = ofRdfNQuads(rdfNQuads);
        return toRdfNQuads(rdfModel);
    }

    /** in-place */
    public static void normalize(Model rdfModel) {
        // remove trivial triples (?s, rdf:type, rdfs:Resource)
        rdfModel.removeAll(null, RDF.type, RDFS.Resource);
    }

    /** in-place */
    @Deprecated
    public static void normalize_OLD(Model rdfModel) {
        // remove trivial triples (?s, rdf:type, rdfs:Resource) if any other (?s, ?p. ?o) is present.
        Set<Resource> redundantResources = new HashSet<>();
        StmtIterator stmts = rdfModel.listStatements(null, RDF.type, RDFS.Resource);
        while (stmts.hasNext()) {
            Resource s = stmts.nextStatement().getSubject();
            // verify with another query
            int c = RdfModels.count(rdfModel, s, null, null, 2);
            if (c > 1) {
                redundantResources.add(s);
            }
        }
        redundantResources.forEach(s -> rdfModel.remove(s, RDF.type, RDFS.Resource));
    }

    public static Model ofRdfNQuads(String rdfNQuads) {
        Model model = ModelFactory.createDefaultModel();
        StringReader sr = new StringReader(rdfNQuads);
        model.read(sr, null, RdfFormats.RdfSyntax.N_QUADS.jenaName);
        return model;
    }

    public static Model ofRdfSyntax(String rdf, RdfFormats.RdfSyntax rdfSyntax) {
        // sameTerm would e..g. treat "123"^^xsd:integer and "123.0"^^xsd:decimal as different terms
        Model model = ModelFactory.createModelSameValue();
        StringReader sr = new StringReader(rdf);
        model.read(sr, null, rdfSyntax.jenaName);
        return model;
    }

    public static RdfFormats.RdfSyntax syntaxFromPathName(String path) {
        for (RdfFormats.RdfSyntax syntax : RdfFormats.RdfSyntax.values()) {
            if (path.startsWith(syntax.resourcePath)) {
                return syntax;
            }
        }
        throw new IllegalArgumentException("No syntax found for path: " + path);
    }

    /** unsorted, but faster */
    public static Stream<String> toRdfNQuadLines(Model rdfModel) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, rdfModel, RDFFormat.NQUADS);
        return Arrays.stream(sw.toString().split("\n")).filter(line -> !line.trim().isEmpty());
    }

    /**
     * This is meant for testing only.
     *
     * @param rdfModel
     * @return
     */
    public static String toRdfNQuads(Model rdfModel) {
        return toRdfNQuadLines(rdfModel).sorted().collect(Collectors.joining("\n")) + "\n";
    }

    public static String toRdfNQuads(Model rdfModel, int maxLines) {
        return toRdfNQuadLines(rdfModel).sorted().limit(maxLines).collect(Collectors.joining("\n")) + "\n";
    }


}
