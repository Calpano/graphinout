package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

class RdfReaderTest {

    private static final Logger log = getLogger(RdfReaderTest.class);

    @Test
    void testReadSimpleRdf() throws IOException {
        String rdfContent = """
                <?xml version="1.0"?>
                <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                         xmlns:ex="http://example.org/">
                  <rdf:Description rdf:about="http://example.org/subject">
                    <ex:predicate rdf:resource="http://example.org/object"/>
                  </rdf:Description>
                </rdf:RDF>
                """;

        SingleInputSource inputSource = SingleInputSource.of("test.rdf", rdfContent);
        RdfReader reader = new RdfReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2document);

        reader.read(inputSource, cjStream);

        ICjDocument cjDoc = cj2document.resultDoc();
        assertNotNull(cjDoc);
        log.info("CJ JSON: " + CjDocuments.toJsonString(cjDoc));

        // Verify document structure
        assertNotNull(cjDoc.theGraph());
        assertTrue(cjDoc.nodes().count() >= 2, "Should have at least 2 nodes");
        assertTrue(cjDoc.edges().count() >= 1, "Should have at least 1 edge");
    }

    @Test
    void testReadTurtle() throws IOException {
        String turtleContent = """
                @prefix ex: <http://example.org/> .

                ex:subject ex:predicate ex:object .
                """;

        SingleInputSource inputSource = SingleInputSource.of("test.ttl", turtleContent);
        RdfReader reader = new RdfReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2document);

        reader.read(inputSource, cjStream);

        ICjDocument cjDoc = cj2document.resultDoc();
        assertNotNull(cjDoc);
        log.info("CJ JSON: " + CjDocuments.toJsonString(cjDoc));

        assertNotNull(cjDoc.theGraph());
        assertTrue(cjDoc.nodes().count() >= 2, "Should have at least 2 nodes");
        assertTrue(cjDoc.edges().count() >= 1, "Should have at least 1 edge");
    }
}
