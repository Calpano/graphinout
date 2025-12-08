package com.graphinout.base.json.json5;

import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JsonReader;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.json5.Json5Preprocessor;
import com.graphinout.foundation.pure.json.writer.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * For each of the files in /test/resources/json, run Json5Preprocessor on it and verify that result is still valid
 * JSON. Also verify that all URLs in source file are still unchanged present.
 */
public class Json5PreprocessorAdvancedTest {

    private static final Path resourceDir = Paths.get("src", "test", "resources", "json");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\d./?=#&%~-]+");

    @DisplayName("Test JSON5 Preprocessor on file")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#jsonResources")
    void testJson5Preprocessor(String displayPath, Resource jsonResource) throws IOException {
        String originalContent = jsonResource.getContentAsString();
        List<String> originalUrls = findUrls(originalContent);

        String processedContent = Json5Preprocessor.toJson(originalContent);

        // 1. Verify that result is still valid JSON
        assertDoesNotThrow(() -> {
            // A simple way to check for valid JSON is to try to parse it.
            JsonReader jsonReader = new JsonReaderImpl();
            InputSource inputSource = SingleInputSourceOfString.of(originalContent, processedContent);
            StringBuilderJsonWriter writer = new StringBuilderJsonWriter();
            jsonReader.read(inputSource, writer);
        }, "Processed content should be valid JSON for " + originalContent);

        // 2. Verify that all URLs in source file are still unchanged present.
        List<String> processedUrls = findUrls(processedContent);
        assertEquals(originalUrls, processedUrls, "URLs should be unchanged after preprocessing " + originalContent);
    }


    private List<String> findUrls(String content) {
        Matcher matcher = URL_PATTERN.matcher(content);
        return matcher.results().map(mr -> mr.group(0)).collect(Collectors.toList());
    }

}
