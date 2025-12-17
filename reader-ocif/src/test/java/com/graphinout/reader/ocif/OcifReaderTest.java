package com.graphinout.reader.ocif;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
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
    @MethodSource("ocifResources")
    @Description("Test JSON->OCIF (all)")
    @Disabled("FIXME")
    void ocif_Cj_Ocif(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            OcifDocument ocifDoc = CjDoc2OcifDoc.toOcifDocument(cjDoc, createErrorHandlerOnLog(log));
            String ocif = OcifDoc2Json.toJsonString(ocifDoc);
            OcifAssert.xAssertThatIsSameOcif(ocif, json, () -> {

                Json2StringWriter json2StringWriter = new Json2StringWriter();
                Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
                ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter);
                try {
                    OcifReader.readOcif("test", json, cjStream, createErrorHandlerOnLog(log));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String cj = json2StringWriter.jsonString();
                log.info("CJ result:\n" + cj);

                Object jaJson = JaJson.parse(ocif);
                String prettyOcif = JsonCompactFormatter.formatCompact(jaJson);
                log.info("OCIF result:\n" + prettyOcif);
            });
        });
        ICjStream cjStream = new CjStream2CjWriter(cjWriter2CjDocumentWriter);
        OcifReader.readOcif("test", json, cjStream, createErrorHandlerOnLog(log));
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
