package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@code cj:isRelated} is how an untyped CJ edge is encoded in RDF (see {@code CjDoc2RdfModel}); on read it
 * must come back as an untyped edge (no type), so that {@code .... → CJ → RDF → CJ → ....} is idempotent.
 */
class RdfIsRelatedTest {

    private static ICjDocument readTurtle(String ttl) throws IOException {
        CjWriter2CjDocumentWriter cj2doc = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2doc, true);
        new RdfTurtleReader().read(SingleInputSource.of("in.ttl", ttl), cjStream);
        return cj2doc.resultDoc();
    }

    private static ICjEdge firstEdge(ICjDocument doc) {
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    @Test
    void cjIsRelatedReadsAsUntypedEdge() throws IOException {
        ICjDocument doc = readTurtle(
                "<http://example.org/a> <http://j-s-o-n.org/connected-json/8.0.0/cj/isRelated> <http://example.org/b> .\n");
        assertNull(firstEdge(doc).type(), "cj:isRelated must read back as an untyped edge");
    }

    @Test
    void ordinaryPredicateStaysTyped() throws IOException {
        ICjDocument doc = readTurtle(
                "<http://example.org/a> <http://example.org/knows> <http://example.org/b> .\n");
        assertNotNull(firstEdge(doc).type(), "a real predicate must remain the edge type");
    }
}
