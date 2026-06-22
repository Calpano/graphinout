package com.graphinout.reader.rdf;

public class RdfFormats {

    public enum RdfSyntax {
        TURTLE("Turtle", "text/turtle", true),//
        N_TRIPLES("N-Triples", "text/n-triples", true),//
        /** as alias for Turtle */
        // N3("N3", "text/notation3"), //
         /** mediatype application/ld+json */
        JSON_LD("JSON-LD", "json/json-ld", false),//
        RDF_XML("RDF/XML", "xml/rdf-xml", false), //
        N_QUADS("N-Quads", "text/rdf-nquads", true), //
        TRIG("TriG", "text/trig", true), //
        /** note: deprecated in favor of JSON-LD */
        RDF_JSON("RDF/JSON", "json/rdf-json", false),//
        /** core TriX format */
        TRIX("TriX", "xml/trix", false);
        public final String jenaName;
        public final String resourcePath;
        /**
         * Whether this syntax can serialise RDF-star (RDF 1.2) triple terms in Jena 5.6. Turtle / TriG /
         * N-Triples / N-Quads emit RDF-star; RDF/XML, JSON-LD (1.1), RDF/JSON and TriX cannot, so edge
         * metadata for these must use classic {@code rdf:Statement} reification instead (plain triples).
         */
        public final boolean supportsRdfStar;

        RdfSyntax(String jenaName, String resourcePath, boolean supportsRdfStar) {
            this.jenaName = jenaName;
            this.resourcePath = resourcePath;
            this.supportsRdfStar = supportsRdfStar;
        }
    }

    public static RdfSyntax detectRdfLanguage(String path, RdfSyntax defaultSyntax) {
        if (path.endsWith(".ttl") || path.endsWith(".ttl.gz")) return RdfSyntax.TURTLE;
        if (path.endsWith(".nt") || path.endsWith(".nt.gz")) return RdfSyntax.N_TRIPLES;
        //if (path.endsWith(".n3") || path.endsWith(".n3.gz")) return RdfSyntax.N3;
        if (path.endsWith(".jsonld") || path.endsWith(".jsonld.gz")) return RdfSyntax.JSON_LD;
        if (path.endsWith(".rdf") || path.endsWith(".rdf.gz")) return RdfSyntax.RDF_XML;
        if (path.endsWith(".rdf.xml") || path.endsWith(".rdf.xml.gz")) return RdfSyntax.RDF_XML;
        if (path.endsWith(".owl") || path.endsWith(".owl.gz")) return RdfSyntax.RDF_XML;
        if (path.endsWith(".nq") || path.endsWith(".nq.gz")) return RdfSyntax.N_QUADS;
        if (path.endsWith(".trig") || path.endsWith(".trig.gz")) return RdfSyntax.TRIG;
        if (path.endsWith(".rj") || path.endsWith(".rj.gz")) return RdfSyntax.RDF_JSON;
        if (path.endsWith(".trix") || path.endsWith(".trix.gz")) return RdfSyntax.TRIX;
        // .foo.gz

        return defaultSyntax;
    }

}
