package com.calpano.graphinout.base.cj;


import com.calpano.graphinout.base.cj.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.impl.DelegatingCjWriter;
import com.calpano.graphinout.base.cj.impl.LoggingCjWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.JsonWriter;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.reader.cj.Json2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.calpano.graphinout.base.cj.impl.CjFormatter.stripCjHeader;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.removeWhitespace;
import static com.google.common.truth.Truth.assertThat;


/**
 *
 */
public class CjTest {

    boolean addLogging = true;
    private Path testResourcesPath;

    static Stream<Arguments> cjFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("src/test/resources/cj");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".cj.json") || p.toString().endsWith(".cj")).map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

    @BeforeEach
    void setUp() {
        testResourcesPath = Paths.get("src/test/resources/cj");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjFileProvider")
    @DisplayName("Test all CJ files together")
    void testAllCjFiles(String displayPath, Path xmlFilePath) throws Exception {
        testCjFile(xmlFilePath);
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
        jsonWriter.string("bar");
        jsonWriter.objectEnd();
        jsonWriter.objectEnd();
        jsonWriter.documentEnd();
    }

    void testCjFile(Path cjFile) throws IOException {
        // read into string
        String json_in = FileUtils.readFileToString(cjFile.toFile(), StandardCharsets.UTF_8);

        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(cjFile.getFileName().toString(), json_in);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter(true);

        /* receive CJ events -> send JSON events  */
        CjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);

        if (addLogging) {
            // insert logging into pipeline
            DelegatingCjWriter cjWriter_out_logging = new DelegatingCjWriter(new LoggingCjWriter());
            cjWriter_out_logging.addJsonWriter(cjWriter_out);
            cjWriter_out = cjWriter_out_logging;
        }

        // HACK
    //    cjWriter_out = new LoggingCjWriter();

        /* receive JSON events -> send CJ events  */
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cjWriter_out);

        // READ JSON -> send JSON events
        jsonReader.read(inputSource, jsonWriter_in);
        String json_out = jsonWriter_out.json();

        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }


}
