package com.graphinout.base.cj;


import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.CjWriter2CjStream;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class Cj2CjStreamTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("JSON<->CjWriter<->CjStream")
    void test_Json_CjWriter_CjStream_CjWriter_Json(String displayPath, Resource xmlResource) throws Exception {
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2jsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2cjWriter = new CjStream2CjWriter(cj2jsonWriter);
        CjWriter2CjStream cjWriter2cjStream = new CjWriter2CjStream(cjStream2cjWriter);
        Json2CjWriter json2cjWriter = new Json2CjWriter(cjWriter2cjStream);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        SingleInputSourceOfString inputSource = TestFileUtil.inputSource(xmlResource);
        jsonReader.read(inputSource, json2cjWriter);

        String json_in = xmlResource.getContentAsString();
        String json_out = json2StringWriter.jsonString();
        CjAssert.xAssertThatIsSameCj(json_out, json_in, null);
    }


}
