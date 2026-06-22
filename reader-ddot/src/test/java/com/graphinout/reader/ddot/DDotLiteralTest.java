package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RDF literals (E1, doc/spec-ddot-rdf.adoc): a triple whose object is flagged literal by inline
 * {@code ,, ..rdf:datatype/language/literal..} metadata becomes a node {@code rdf:data} property (so it
 * round-trips as a real RDF literal), NOT an edge to a resource node.
 */
class DDotLiteralTest {

    private static final String XSD_INT = "http://www.w3.org/2001/XMLSchema#integer";

    private static ICjDocument parse(String ddot) throws IOException {
        return DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
    }

    private static IJsonValue rdfData(ICjDocument doc, String nodeId) {
        return doc.graphs().findFirst().orElseThrow()
                .nodes().filter(n -> nodeId.equals(n.id())).findFirst().orElseThrow()
                .data().jsonValue().asObject().get("rdf:data");
    }

    @Test
    void plainTypedAndLangLiteralsBecomeNodeDataNotEdges() throws IOException {
        ICjDocument doc = parse("""
                x ..foaf:name.. Alice ,, ..rdf:literal.. true
                x ..foaf:age.. 30 ,, ..rdf:datatype.. http://www.w3.org/2001/XMLSchema#integer
                x ..rdfs:label.. Bonjour ,, ..rdf:language.. fr
                x ..foaf:knows.. y
                """);

        IJsonValue rd = rdfData(doc, "x");
        assertEquals("Alice", rd.asObject().get("foaf:name").asString(), "plain literal = bare string");
        assertEquals("30", rd.asObject().get("foaf:age").asObject().get("value").asString());
        assertEquals(XSD_INT, rd.asObject().get("foaf:age").asObject().get("datatype").asString());
        assertEquals("Bonjour", rd.asObject().get("rdfs:label").asObject().get("value").asString());
        assertEquals("fr", rd.asObject().get("rdfs:label").asObject().get("language").asString());

        // only the unflagged triple is a real edge; literal objects are not nodes
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertEquals(1, g.edges().count(), "only foaf:knows is an edge");
        assertTrue(g.nodes().noneMatch(n -> "Alice".equals(n.id())), "a literal value is not a node");
        assertTrue(g.nodes().anyMatch(n -> "y".equals(n.id())), "a resource object IS a node");
    }

    @Test
    void literalsRoundTripIncludingMultiValued() throws IOException {
        String ddot = """
                x ..foaf:name.. Alice ,, ..rdf:literal.. true
                x ..foaf:age.. 30 ,, ..rdf:datatype.. http://www.w3.org/2001/XMLSchema#integer
                x ..tag.. red ,, ..rdf:literal.. true
                x ..tag.. blue ,, ..rdf:literal.. true
                """;
        ICjDocument cj1 = parse(ddot);
        String ddot2 = new DDotOutput(cj1).toDDot();
        ICjDocument cj2 = parse(ddot2);
        assertEquals(CjDocuments.toJsonString(cj1), CjDocuments.toJsonString(cj2),
                () -> "literals must round-trip; regenerated ddot:\n" + ddot2);

        // the multi-valued literal accumulated into an array
        assertEquals(2, rdfData(cj1, "x").asObject().get("tag").asArray().size());
    }
}
