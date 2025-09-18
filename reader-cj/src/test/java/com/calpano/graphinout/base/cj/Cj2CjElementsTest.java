package com.calpano.graphinout.base.cj;


import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.impl.Cj2JsonWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.reader.cj.Json2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.calpano.graphinout.base.cj.impl.CjFormatter.stripCjHeader;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.removeWhitespace;
import static com.google.common.truth.Truth.assertThat;


/**
 *
 */
public class Cj2CjElementsTest {

    boolean addLogging = true;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.base.cj.CjFileProvider#cjFileProvider")
    @DisplayName("Test all CJ files together")
    void test_Json2Cj2Elements2Cj2Json(String displayPath, Path xmlFilePath) throws Exception {
        // == OUT Pipeline
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
        // == IN Pipeline
        String json_in = FileUtils.readFileToString(xmlFilePath.toFile(), StandardCharsets.UTF_8);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(xmlFilePath.getFileName().toString(), json_in);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        // == READ JSON -> send JSON events
        jsonReader.read(inputSource, jsonWriter_in);

        ICjDocument doc = cj2ElementsWriter.resultDoc();
        StringBuilderJsonWriter jsonWriter = new StringBuilderJsonWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(jsonWriter);
        doc.fire(cj2JsonWriter);

        String json_out = jsonWriter.json();
        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }


}
