package com.calpano.graphinout.base.cj;


import com.calpano.graphinout.base.cj.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.impl.DelegatingCjWriter;
import com.calpano.graphinout.base.cj.impl.LoggingCjWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.reader.cj.Cj2GioWriter;
import com.calpano.graphinout.reader.cj.Gio2CjWriter;
import com.calpano.graphinout.reader.cj.Json2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class CjGioTest {

    boolean addLogging = true;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.base.cj.CjFileProvider#cjFileProvider")
    @DisplayName("Test JSON-CJ-JSON - all files together")
    void test_json_cj_json_AllCjFiles(String displayPath, Path path) throws Exception {
        String json_in = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(path.getFileName().toString(), json_in);
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        /* receive CJ events -> send JSON events  */
        CjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);


        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cjWriter_out);
        jsonReader.read(inputSource, jsonWriter_in);


        String json_out = jsonWriter_out.json();
        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.base.cj.CjFileProvider#cjFileProvider")
    @DisplayName("Test JSON-CJ-GIO-CJ-JSON - all files together")
    void test_json_cj_gio_cj_json_AllCjFiles(String displayPath, Path path) throws Exception {
        String json_in = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(path.getFileName().toString(), json_in);
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        /* receive CJ events -> send JSON events  */
        CjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);
        if (addLogging) {
            // insert logging into pipeline
            DelegatingCjWriter cjWriter_out_logging = new DelegatingCjWriter(new LoggingCjWriter());
            cjWriter_out_logging.addJsonWriter(cjWriter_out);
            cjWriter_out = cjWriter_out_logging;
        }

        /* receive GIO events -> send CJ events  */
        Gio2CjWriter gio2cjWriter = new Gio2CjWriter(cjWriter_out);
        /* receive CJ events -> send GIO events  */
        Cj2GioWriter gioWriter_in = new Cj2GioWriter(gio2cjWriter);
        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(gioWriter_in);
        // == READ JSON -> send JSON events
        jsonReader.read(inputSource, jsonWriter_in);


        String json_out = jsonWriter_out.json();
        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }

    @Test
    void test() {
        JsonWriter jsonWriter = new Json2CjWriter(new LoggingCjWriter(false));
        jsonWriter.documentStart();
        // simulate { "data": { "foo":  "bar" } }
        jsonWriter.objectStart();
        jsonWriter.onKey("data");
        jsonWriter.objectStart();
        jsonWriter.onKey("foo");
        jsonWriter.onString("bar");
        jsonWriter.objectEnd();
        jsonWriter.objectEnd();
        jsonWriter.documentEnd();
    }


}
