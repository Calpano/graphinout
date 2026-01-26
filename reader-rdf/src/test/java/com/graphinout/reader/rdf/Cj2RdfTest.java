package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

class Cj2RdfTest {

    private static final Logger log = getLogger(Cj2RdfTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON-Canonical CJ-JSON - all files together")
    void testCj2Rdf_AllCj(String displayPath, Resource resource) throws Exception {
        String cjJson = resource.getContentAsString();
        ICjDocument cjDoc = CjDocuments.parseCjJsonString(displayPath, cjJson);
        assertNotNull(cjDoc);

        // CJ to RDF
        RdfWriter rdfWriter = new RdfWriter();
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        rdfWriter.write(cjDoc, sink);

        String resultRdf = sink.getBufferAsUtf8String();
        log.info("Result RDF:\n" + resultRdf);

        assertFalse(resultRdf.isEmpty(), "RDF output should not be empty");
    }

    @Test
    void test() throws Exception {
        String path = "json/cj_7_0_0/example-cj-with-all-features.cj.json";
        TestFileProvider.TestResource res = TestFileProvider.resourceByPath(path);
        assertThat(res).isNotNull();
        testCj2Rdf_AllCj(path, res.resource());
    }


}
