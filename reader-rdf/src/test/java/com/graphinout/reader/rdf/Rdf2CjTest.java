package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

class Rdf2CjTest {

    private static final Logger log = getLogger(Rdf2CjTest.class);


    @Test
    void testRdf2Cj() throws IOException {
        Resource res = TestFileUtil.resource("text/rdf/test.ttl");
        assertNotNull(res, "Resource not found");

        // RDF to CJ
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        RdfReader reader = new RdfReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2document, true);

        reader.read(inputSource, cjStream);

        ICjDocument cjDoc = cj2document.resultDoc();
        assertNotNull(cjDoc);
        String cjJson = CjDocuments.toJsonString(cjDoc);

        Resource expected = TestFileUtil.expectedResourceWithExtension(res, "rdf2cj", ".cj.json");
        assertNotNull(expected);
        String expectedCj = expected.getContentAsString();
        CjAssert.xAssertThatIsSameCj(cjJson, expectedCj, null);
    }


}
