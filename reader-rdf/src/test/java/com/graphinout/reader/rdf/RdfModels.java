package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RdfModels {

    public static String normalize(String rdfNQuads) {
        Model rdfModel = ofRdfNQuads(rdfNQuads);
        return toRdfNQuads(rdfModel);
    }

    public static Model ofRdfNQuads(String rdfNQuads) {
        Model model = ModelFactory.createDefaultModel();
        StringReader sr = new StringReader(rdfNQuads);
        model.read(sr, null, RdfFormats.RdfSyntax.N_QUADS.jenaName);
        return model;
    }

    public static Model ofRdfSyntax(String rdf, RdfFormats.RdfSyntax rdfSyntax) {
        Model model = ModelFactory.createDefaultModel();
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

    /**
     * This is meant for testing only.
     *
     * @param rdfModel
     * @return
     */
    public static String toRdfNQuads(Model rdfModel) {

        Model canon = RdfCanonical.canonicalBlankNodes(rdfModel);

        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, canon, RDFFormat.NQUADS);
        return Arrays.stream(sw.toString().split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .filter(line ->!isTrivial(line))
                .sorted()
                .collect(Collectors.joining("\n")) + "\n";
    }

    private static boolean isTrivial( String nquadsLine) {
        if(nquadsLine.endsWith("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Resource> ."))
            return true;

        return false;
    }

}
