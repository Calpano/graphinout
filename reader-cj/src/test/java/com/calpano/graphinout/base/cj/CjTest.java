package com.calpano.graphinout.base.cj;


import com.calpano.graphinout.base.cj.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.impl.DelegatingCjWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.JsonWriter;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.reader.cj.Json2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


/**
 *
 */
public class CjTest {

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

    void testCjFile(Path cjFile) throws IOException {
        // read into string
        String in = FileUtils.readFileToString(cjFile.toFile(), StandardCharsets.UTF_8);

        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(cjFile.getFileName().toString(), in);
        JsonReaderImpl jsonReader = new JsonReaderImpl();

        // build pipeline, reverse
        // collect the CJ stream back into JSON
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter(true);
        Cj2JsonWriter cj2jsonWriter = new Cj2JsonWriter(jsonWriter_out);

        DelegatingCjWriter cjWriter_out = new DelegatingCjWriter(cj2jsonWriter);
//        LoggingCjWriter cj2log = new LoggingCjWriter();
//        cjWriter_out.addWriter(cj2log);

        JsonWriter json2cjWriter = new Json2CjWriter(cj2jsonWriter);
        // trigger reading: read from input source and push to JsonWriter
        jsonReader.read(inputSource, cjWriter_out);
    }


}
