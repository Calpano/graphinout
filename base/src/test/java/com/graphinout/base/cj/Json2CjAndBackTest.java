package com.graphinout.base.cj;

import com.graphinout.base.cj.util.CjNormalizer;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.text.JsonFormatting;
import com.graphinout.foundation.pure.json.writer.impl.ValidatingJsonWriter;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.DelegatingJsonWriter;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.writer.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.base.TestFileUtil2.inputSource;
import static com.graphinout.foundation.pure.text.JsonFormatting.formatDebug;
import static com.graphinout.foundation.pure.text.JsonFormatting.removeWhitespace;

public class Json2CjAndBackTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON-Canonical CJ-JSON - all files together")
    void test_json_cj_json_AllCj(String displayPath, Resource resource) throws Exception {
        SingleInputSourceOfString inputSource = inputSource(resource);
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();

        DelegatingJsonWriter delegatingJsonWriter = new DelegatingJsonWriter(new ValidatingJsonWriter());
        delegatingJsonWriter.addJsonWriter(jsonWriter_out);

        /* receive CJ events -> send JSON events  */
        ICjWriter cjWriter_out = new Cj2JsonWriter(delegatingJsonWriter);

        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cjWriter_out);
        jsonReader.read(inputSource, jsonWriter_in);


        String json_in = resource.getContentAsString();
        String json_out = jsonWriter_out.json();
        CjAssert.xAssertThatIsSameCj(json_out, json_in,  ()->{});
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON-Canonical CJ - all files together")
    void test_json_cjCanonicalizeInput(String displayPath, Resource resource) throws Exception {
        SingleInputSourceOfString inputSource = inputSource(resource);
        JsonReaderImpl jsonReader = new JsonReaderImpl();

        String json = resource.getContentAsString();
        // test 1
        removeWhitespace(json);
        // test 2
        CjNormalizer.canonicalize(json);
    }

}
