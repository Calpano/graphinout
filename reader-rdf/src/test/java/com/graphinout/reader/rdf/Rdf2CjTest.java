package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

class Rdf2CjTest {

    private static final Logger log = getLogger(Rdf2CjTest.class);
    static Map<String, RdfReader> prefix_reader = new HashMap<>();

    public static Stream<TestFileProvider.TestResource> rdfResources() {
        return Stream.of(RdfFormats.RdfSyntax.values()).flatMap(Rdf2CjTest::rdfResourcesWithSyntax);
    }

    public static Stream<TestFileProvider.TestResource> rdfResourcesWithSyntax(RdfFormats.RdfSyntax syntax) {
        return TestFileProvider.resources(syntax.resourcePath, Set.of());
    }

    @BeforeAll
    public static void setUp() {
        new RdfService().readers().stream().map(r -> (RdfReader) r).forEach(reader -> prefix_reader.put(reader.rdfSyntax().resourcePath, reader));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("rdfResources")
    @DisplayName("RDF -> CJ")
    void testRdf2Cj(String displayName, Resource res) throws IOException {
        assertNotNull(res, "Resource not found");
        rdf2cj(res);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("rdfResources")
    @DisplayName("RDF <-> CJ")
    void testRdf2Cj2Rdf(String displayName, Resource res) throws IOException {
        assertNotNull(res, "Resource not found");
        ICjDocument cjDoc = rdf2cj(res);
        Model rdfModelActual = CjDoc2RdfModel.cjDoc2Model(cjDoc);
        RdfFormats.RdfSyntax rdfSyntax = RdfModels.syntaxFromPathName(res.getPath());
        Model rdfModelExpected = RdfModels.ofRdfSyntax(res.getContentAsString(), rdfSyntax);
        RdfAssert.xAssertThatIsSameRdf(rdfModelActual, rdfModelExpected, () -> {
            log.info("Resource: "+res.getURL());
            log.info("----CJ:\n" + CjDocuments.toJsonString(cjDoc));
        });
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("rdfResources")
    @DisplayName("RDF test resource parsing")
    void testRdfResourceParsing(String displayName, Resource res) throws IOException {
        assertNotNull(res, "Resource not found");
        RdfFormats.RdfSyntax rdfSyntax = RdfModels.syntaxFromPathName(res.getPath());
        assertThat(rdfSyntax).isNotNull();
        Model rdfModelExpected = RdfModels.ofRdfSyntax(res.getContentAsString(), rdfSyntax);
    }

    private ICjDocument rdf2cj(Resource res) throws IOException {
        RdfReader reader = readerForPath(res.getPath());

        // RDF to CJ
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream = new CjStream2CjWriter(cj2document, true);

        reader.read(inputSource, cjStream);

        ICjDocument cjDoc = cj2document.resultDoc();
        assertNotNull(cjDoc);
        String cjJson = CjDocuments.toJsonString(cjDoc);

        Resource expected = TestFileUtil.expectedResourceWithExtension(res, "rdf2cj", ".cj.json");
        if (expected != null) {
            String expectedCj = expected.getContentAsString();
            CjAssert.xAssertThatIsSameCj(cjJson, expectedCj, null);
        }
        return cjDoc;
    }

    private RdfReader readerForPath(String path) {
        for (Map.Entry<String, RdfReader> entry : prefix_reader.entrySet()) {
            String p = entry.getKey();
            if (path.startsWith(p)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No reader found for path: " + path);
    }


}
