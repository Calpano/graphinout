package com.calpano.graphinout.base.cj;


import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.impl.JsonFormatter;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.google.common.truth.Truth.assertThat;


/**
 *
 */
public class Cj2CjElementsTest {

    boolean addLogging = true;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test all Canonical CJ files together")
    void test_Json2Cj2Elements2Cj2Json(String displayPath, Resource xmlResource) throws Exception {
        // == OUT Pipeline
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
        // == IN Pipeline
        SingleInputSourceOfString inputSource = TestFileUtil.inputSource(xmlResource);

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        // == READ JSON -> send JSON events
        jsonReader.read(inputSource, jsonWriter_in);

        ICjDocument doc = cj2ElementsWriter.resultDoc();
        StringBuilderJsonWriter jsonWriter = new StringBuilderJsonWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(jsonWriter);
        doc.fire(cj2JsonWriter);

        String json_in = xmlResource.getContentAsString();
        String json_out = jsonWriter.json();
        assertThat(formatDebug(CjNormalizer.canonicalize(JsonFormatter.removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(CjNormalizer.canonicalize(JsonFormatter.removeWhitespace(json_in))));
    }


}
