package com.graphinout.base.cj;


import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.util.CjNormalizer;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.util.JsonFormatter;
import com.graphinout.foundation.json.writer.JsonWriter;
import com.graphinout.foundation.json.JsonReaderImpl;
import com.graphinout.foundation.json.writer.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.json.util.JsonFormatter.formatDebug;


/**
 *
 */
public class Cj2CjElementsTest {

    boolean addLogging = true;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test all Canonical CJ files together")
    void test_Json2Cj2Elements2Cj2Json(String displayPath, Resource xmlResource) throws Exception {
        // == OUT Pipeline
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
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
