package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Datatype IRIs in RDF literals must be CURIE-abbreviated symmetrically with node ids and predicates:
 * abbreviated to a CURIE on RDF&rarr;CJ read (using the document {@code @context}) and expanded back to the
 * full IRI on CJ&rarr;RDF write. See {@code doc/spec-ddot-rdf.adoc} &sect; "Datatype IRI abbreviation".
 */
class RdfDatatypeAbbreviationTest {

    private static final Logger log = getLogger(RdfDatatypeAbbreviationTest.class);

    private static ICjDocumentMutable turtle2cj(String turtle) {
        Model model = RdfModels.ofRdfSyntax(turtle, RdfFormats.RdfSyntax.TURTLE);
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        RdfModel2CjDoc.rdfModel2cjDoc(model, cjDoc, null);
        return cjDoc;
    }

    /**
     * Datatype whose prefix IS in scope (xsd): CJ must store the CURIE {@code xsd:integer}, NOT the full
     * XSD IRI; and the RDF&rarr;CJ&rarr;RDF round-trip must reproduce the original {@code "30"^^xsd:integer}.
     */
    @Test
    void xsdDatatypeAbbreviatedAndRoundTrips() {
        String turtle = """
                @prefix ex:  <http://example.org/> .
                @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
                ex:alice ex:age "30"^^xsd:integer .
                """;
        Model expected = RdfModels.ofRdfSyntax(turtle, RdfFormats.RdfSyntax.TURTLE);

        ICjDocumentMutable cjDoc = turtle2cj(turtle);
        String cjJson = CjDocuments.toJsonString(cjDoc);
        log.info("CJ JSON:\n{}", cjJson);

        // intermediate CJ stores the CURIE, not the full XSD IRI
        assertThat(cjJson).contains("\"datatype\":\"xsd:integer\"");
        assertThat(cjJson).doesNotContain("http://www.w3.org/2001/XMLSchema#integer");

        // CJ -> RDF expands the CURIE back to the full IRI; round-trip is isomorphic
        Model actual = CjDoc2RdfModel.cjDoc2Model(cjDoc);
        RdfModels.normalize(actual);
        RdfModels.normalize(expected);
        RdfAssert.xAssertThatIsSameRdf(actual, expected, () -> log.info("CJ:\n{}", cjJson));
    }

    /**
     * Custom-namespace datatype whose prefix IS in scope (ex:): CJ stores the CURIE {@code ex:myType} and
     * the round-trip reproduces the typed literal.
     */
    @Test
    void customDatatypeAbbreviatedAndRoundTrips() {
        String turtle = """
                @prefix ex: <http://example.org/> .
                ex:s ex:p "x"^^ex:myType .
                """;
        Model expected = RdfModels.ofRdfSyntax(turtle, RdfFormats.RdfSyntax.TURTLE);

        ICjDocumentMutable cjDoc = turtle2cj(turtle);
        String cjJson = CjDocuments.toJsonString(cjDoc);
        log.info("CJ JSON:\n{}", cjJson);

        assertThat(cjJson).contains("\"datatype\":\"ex:myType\"");
        assertThat(cjJson).doesNotContain("http://example.org/myType");

        Model actual = CjDoc2RdfModel.cjDoc2Model(cjDoc);
        RdfModels.normalize(actual);
        RdfModels.normalize(expected);
        RdfAssert.xAssertThatIsSameRdf(actual, expected, () -> log.info("CJ:\n{}", cjJson));
    }

    /**
     * Datatype with NO matching prefix in scope stays a full IRI in CJ (asId_ is a no-op) and still
     * round-trips (expandId is a no-op on a full IRI).
     */
    @Test
    void unprefixedDatatypeStaysFullIriAndRoundTrips() {
        // only ex: is declared; the datatype namespace http://other.example/ has no prefix
        String turtle = """
                @prefix ex: <http://example.org/> .
                ex:s ex:p "x"^^<http://other.example/customType> .
                """;
        Model expected = RdfModels.ofRdfSyntax(turtle, RdfFormats.RdfSyntax.TURTLE);

        ICjDocumentMutable cjDoc = turtle2cj(turtle);
        String cjJson = CjDocuments.toJsonString(cjDoc);
        log.info("CJ JSON:\n{}", cjJson);

        // no prefix to abbreviate against -> stays a full IRI
        assertThat(cjJson).contains("\"datatype\":\"http://other.example/customType\"");

        Model actual = CjDoc2RdfModel.cjDoc2Model(cjDoc);
        RdfModels.normalize(actual);
        RdfModels.normalize(expected);
        RdfAssert.xAssertThatIsSameRdf(actual, expected, () -> log.info("CJ:\n{}", cjJson));
    }
}
