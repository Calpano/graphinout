package com.graphinout.reader.cj;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.element.CjDataSchema;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjHasData;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;

public class CjTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("JSON->CjDoc->Analysis")
    void testCjAnalysis(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        ICjDocument doc = ConnectedJsonReader.readToDocument(json);
        assertThat(doc).isNotNull();
        long datas = doc.allElements().filter(elem -> elem instanceof ICjHasData) //
                .map(elem -> (ICjHasData) elem)//
                .map(ICjHasData::data)//
                .filter(Objects::nonNull)//
                .map(ICjData::jsonValue)//
                .filter(Objects::nonNull)//
                .count();
        if (datas == 0) return;

        CjDataSchema schema = CjDocuments.calcEffectiveSchemaForData(doc);
        assertThat(schema.map()).isNotEmpty();
    }


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON<->CJ<->JSON (all)")
    void test_Json_Cj_Json(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        JsonWriter json2cjWriter = Json2CjWriter.createWritingTo(cj2JsonWriter);

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);
        jsonReader.read(inputSource, json2cjWriter);
        String resultJson = json2StringWriter.jsonString();

        CjAssert.xAssertThatIsSameCj(resultJson, json, null);
    }

}
