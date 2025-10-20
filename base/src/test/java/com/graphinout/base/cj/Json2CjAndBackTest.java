package com.graphinout.base.cj;

import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.impl.JsonFormatter;
import com.graphinout.foundation.json.impl.ValidatingJsonWriter;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.DelegatingJsonWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.TestFileUtil.inputSource;
import static com.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.graphinout.foundation.json.impl.JsonFormatter.removeWhitespace;

public class Json2CjAndBackTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
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
        String json_outNormWhitespace = removeWhitespace(json_out);
        String json_outNormHeader = CjNormalizer.canonicalize(json_outNormWhitespace);
        String json_outNormFormat = formatDebug(json_outNormHeader);
        assertThat(json_outNormFormat).isEqualTo(formatDebug(CjNormalizer.canonicalize(JsonFormatter.removeWhitespace(json_in))));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
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
