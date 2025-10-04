package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.calpano.graphinout.base.cj.CjFormatter.stripCjHeader;
import static com.calpano.graphinout.foundation.TestFileUtil.inputSource;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.removeWhitespace;
import static com.google.common.truth.Truth.assertThat;

public class Json2CjAndBackTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON-Canonical CJ-JSON - all files together")
    void test_json_cj_json_AllCj(String displayPath, Resource resource) throws Exception {
        SingleInputSourceOfString inputSource = inputSource(resource);
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        /* receive CJ events -> send JSON events  */
        ICjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);


        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cjWriter_out);
        jsonReader.read(inputSource, jsonWriter_in);


        String json_in = resource.getContentAsString();
        String json_out = jsonWriter_out.json();
        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }

}
