package com.graphinout.reader.rdf;

public class RdfFormats {

    public enum RdfSyntax {
        TURTLE("Turtle", "text/turtle"),//
        N_TRIPLES("N-Triples", "text/n-triples"),//
        /** as alias for Turtle */
        // N3("N3", "text/notation3"), //
         /** mediatype application/ld+json */
        JSON_LD("JSON-LD", "json/json-ld"),//
        RDF_XML("RDF/XML", "xml/rdf-xml"), //
        N_QUADS("N-Quads", "text/rdf-nquads"), //
        TRIG("TriG", "text/trig"), //
        /** note: deprecated in favor of JSON-LD */
        RDF_JSON("RDF/JSON", "json/rdf-json"),//
        /** core TriX format */
        TRIX("TriX", "xml/trix");
        public final String jenaName;
        public final String resourcePath;

        RdfSyntax(String jenaName, String resourcePath) {
            this.jenaName = jenaName;
            this.resourcePath = resourcePath;
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
