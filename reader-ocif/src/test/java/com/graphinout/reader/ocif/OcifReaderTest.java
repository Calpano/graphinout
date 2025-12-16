package com.graphinout.reader.ocif;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import com.graphinout.reader.ocif.todo.CjStream2OcifJson;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
    void test_Json_Cj_Json(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();

        CjStream2OcifJson cjStream2Ocif = new CjStream2OcifJson();
        OcifReader.readOcif("test", json, cjStream2Ocif, createErrorHandlerOnLog(log));
        String resultJson = cjStream2Ocif.resultOcifJsonString();

        CjAssert.xAssertThatIsSameCj(resultJson, json, null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Test JSON->OCIF (all)")
    void test_OCIF_Cj_OCIF(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        CjStream2OcifJson ocif2CjStream = new CjStream2OcifJson();
        OcifReader.readOcif("test", json, ocif2CjStream, createErrorHandlerOnLog(log));
        String ocif = ocif2CjStream.resultOcifJsonString();
        OcifAssert.xAssertThatIsSameOcif(ocif, json, ()->{
            // for debug
            Object jaJson = JaJson.parse(ocif);
            String prettyOcif = JsonCompactFormatter.formatCompact(jaJson);
            log.info("OCIF result:\n"+prettyOcif);
        });
    }

}
