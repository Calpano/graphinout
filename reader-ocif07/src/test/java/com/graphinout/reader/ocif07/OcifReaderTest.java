package com.graphinout.reader.ocif07;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.cj.CjDoc2OcifDoc;
import com.graphinout.reader.ocif07.cj.OcifDoc2CjDoc;
import com.graphinout.reader.ocif07.document.IOcifDocument;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.testdata.TestFileProvider.resources;
import static org.slf4j.LoggerFactory.getLogger;

class OcifReaderTest extends AbstractReaderTest {

    private static final Logger log = getLogger(OcifReaderTest.class);

    public static Stream<TestFileProvider.TestResource> ocifResources() {
        return resources("json/ocif", Set.of(".ocif", ".ocif.json"));
    }

    @Override
    protected List<GioReader> readersToTest() {
        return List.of(new OcifReader());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.base.cj.CjDocsTestData#cjTestDocs")
    @Description("Test CJ doc->OCIF doc->CJ doc->OCIF doc (all)")
    void cjDoc_ocifDoc_CjDoc_ocifDoc(String displayName, ICjDocument cjDoc) throws IOException {
        OcifDocument ocifDoc = CjDoc2OcifDoc.toOcifDocument(cjDoc, createErrorHandlerOnLog(log));
        assertThat(ocifDoc).isNotNull();
        String ocifJson = OcifDoc2Json.toJsonString(ocifDoc);

        ICjDocument cjDoc_out = OcifDoc2CjDoc.toCjDocument(ocifDoc);
        OcifDocument ocifDoc_out = CjDoc2OcifDoc.toOcifDocument(cjDoc_out, createErrorHandlerOnLog(log));
        String ocifJson2 = OcifDoc2Json.toJsonString(ocifDoc_out);

        OcifAssert.xAssertThatIsSameOcif(ocifJson2, ocifJson, () -> {
            log.info("CJ in:\n" + cjDoc.toJsonFormatted());
            log.info("OCIF in:\n" + IOcifDocument.toJsonValue(ocifDoc).toJsonFormatted());
            log.info("CJ out:\n" + cjDoc_out.toJsonFormatted());
        });
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Test OCIF/json->OCIF/doc->CJ/doc->OCIF/doc->OCIF/json")
    void ocif_Cj_Ocif(String displayName, Resource resource) throws IOException {
        if (TestFileUtil.isExpected(resource))
            return;
        String ocifJson_in = resource.getContentAsString();
        IJsonValue ocifJsonValue = JavaJsons.ofJsonString(ocifJson_in);

        List<ContentError> contentErrors = new ArrayList<>();
        OcifDocument ocifDoc = Json2OcifDoc.toOcifDocument(ocifJsonValue, contentErrors::add);
        ICjDocument cjDoc = OcifDoc2CjDoc.toCjDocument(ocifDoc);
        OcifDocument ocifDoc_out = CjDoc2OcifDoc.toOcifDocument(cjDoc, contentErrors::add);
        String ocifJson_out = OcifDoc2Json.toJsonString(ocifDoc_out);

        Resource expectedCj = TestFileUtil.expectedResource(resource, "ocif2cj");
        if (expectedCj != null) {
            String cjDoc_in = expectedCj.getContentAsString();
            String cjJson_out = CjDocuments.toJsonString(cjDoc);
            CjAssert.xAssertThatIsSameCj(cjJson_out, cjDoc_in, null);
        }

        OcifAssert.xAssertThatIsSameOcif(ocifJson_out, ocifJson_in, () -> {
            log.info("CJ:\n" + cjDoc.toJsonFormatted());
        });

        assertThat(contentErrors.stream().filter(ce -> ce.level == ContentError.ErrorLevel.Error)).isEmpty();

        // TODO test with actual reader
//
//        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
//            OcifDocument ocifDoc = CjDoc2OcifDoc.cjDocumentToOcifDocument(cjDoc, createErrorHandlerOnLog(log));
//            String ocifJson_out = OcifDoc2Json.toJsonString(ocifDoc);
//
//            String actualWrapped = OcifAssert.normalize(ocifJson_out, 60);
//            String expectedWrapped = OcifAssert.normalize(ocifJson_in, 60);
//
//            if (!actualWrapped.equals(expectedWrapped)) {
//                Json2StringWriter json2StringWriter = new Json2StringWriter();
//                Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
//                ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter);
//                try {
//                    OcifReader.readOcif("test", ocifJson_in, cjStream, createErrorHandlerOnLog(log));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                String cj = json2StringWriter.jsonString();
//                log.info("CJ result:\n" + cj);
//
//                Object jaJson = JaJson.parse(ocifJson_out);
//                String prettyOcif = JsonCompactFormatter.formatCompact(jaJson);
//                log.info("OCIF result:\n" + prettyOcif);
//            }
//            assertThat(actualWrapped).isEqualTo(expectedWrapped);
//        });
//        ICjStream cjStream = new CjStream2CjWriter(cjWriter2CjDocumentWriter);
//        OcifReader.readOcif("test", ocifJson_in, cjStream, createErrorHandlerOnLog(log));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Test JSON->OCIF (all)")
    void ocif_OcifDoc(String displayName, Resource resource) throws IOException {
        String ocifJson_in = resource.getContentAsString();
        IJsonValue jsonValue = JavaJsons.ofJsonString(ocifJson_in);
        OcifDocument ocifDoc = Json2OcifDoc.toOcifDocument(jsonValue, createErrorHandlerOnLog(log));
        assertThat(ocifDoc).isNotNull();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Test JSON->OCIF->JSON (all)")
    void ocif_OcifDoc_Ocif(String displayName, Resource resource) throws IOException {
        String ocifJson_in = resource.getContentAsString();
        IJsonValue jsonValue = JavaJsons.ofJsonString(ocifJson_in);
        OcifDocument ocifDoc = Json2OcifDoc.toOcifDocument(jsonValue, createErrorHandlerOnLog(log));
        String ocifJson_out = OcifDoc2Json.toJsonString(ocifDoc);

        OcifAssert.xAssertThatIsSameOcif(ocifJson_out, ocifJson_in, null);
    }

}
