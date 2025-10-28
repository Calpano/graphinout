package com.graphinout.reader.ocif;

import com.graphinout.base.AbstractReaderTest;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.GioReader;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.graphinout.foundation.TestFileProvider.resources;

class OcifReaderTest extends AbstractReaderTest {

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
    void test_Json_Cj_Json(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);

        OcifReader ocifReader = new OcifReader();
        Ocif2CjStream ocif2CjStream = new Ocif2CjStream();
        ocifReader.read(inputSource, ocif2CjStream);
        String resultJson = ocif2CjStream.resultOcifJsonString();

        CjAssert.xAssertThatIsSameCj(resultJson, json, null);
    }

}
