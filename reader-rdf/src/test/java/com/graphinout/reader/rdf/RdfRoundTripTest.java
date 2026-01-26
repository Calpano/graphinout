package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

class RdfRoundTripTest {

    private static final Logger log = getLogger(RdfRoundTripTest.class);

    @Test
    void testRoundTrip() throws IOException {
        Resource res = TestFileUtil.resource("text/rdf/test.ttl");
        assertNotNull(res, "Resource not found");

        // RDF to CJ
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        RdfReader reader = new RdfTurtleReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2document, true);

        reader.read(inputSource, cjStream);

        ICjDocument cjDoc = cj2document.resultDoc();
        assertNotNull(cjDoc);
        String cjJson = CjDocuments.toJsonString(cjDoc);
        log.info("CJ JSON: " + cjJson);

        // CJ to RDF
        RdfTurtleReader writer = new RdfTurtleReader();
        InMemoryOutputSink sink = InMemoryOutputSink.create();

        ICjStream cjStream_out = writer.createCjStream(sink);
        ICjWriter cjWriter = new CjWriter2CjStream(cjStream_out);
        cjDoc.fire(cjWriter, false);

        String resultRdf = sink.getBufferAsUtf8String();
        log.info("Result RDF:\n" + resultRdf);

        assertFalse(resultRdf.isEmpty(), "RDF output should not be empty");
        assertTrue(resultRdf.contains("knows") || resultRdf.contains("http://example.org/knows"), "RDF should contain the 'knows' predicate");
    }

}
