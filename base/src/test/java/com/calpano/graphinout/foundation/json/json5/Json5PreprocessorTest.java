package com.calpano.graphinout.foundation.json.json5;

import com.calpano.graphinout.foundation.json.JsonReader;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.calpano.graphinout.foundation.TestFileUtil.file;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * For each of the files in /test/resources/json, run Json5Preprocessor on it and verify that result is still valid
 * JSON. Also verify that all URLs in source file are still unchanged present.
 */
public class Json5PreprocessorTest {

    private static final Path resourceDir = Paths.get("src", "test", "resources", "json");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\d./?=#&%~-]+");

    @DisplayName("Test JSON5 Preprocessor on file")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#jsonResources")
    void testJson5Preprocessor(String displayPath, Resource jsonResource) throws IOException {
        File jsonFile = file(jsonResource);
        String originalContent = Files.readString(jsonFile.toPath());
        List<String> originalUrls = findUrls(originalContent);

        String processedContent = Json5Preprocessor.toJson(originalContent);

        // 1. Verify that result is still valid JSON
        assertDoesNotThrow(() -> {
            // A simple way to check for valid JSON is to try to parse it.
            JsonReader jsonReader = new JsonReaderImpl();
            com.calpano.graphinout.foundation.input.InputSource inputSource = com.calpano.graphinout.foundation.input.SingleInputSourceOfString.of(jsonFile.toString(), processedContent);
            StringBuilderJsonWriter writer = new StringBuilderJsonWriter();
            jsonReader.read(inputSource, writer);
        }, "Processed content should be valid JSON for " + jsonFile);

        // 2. Verify that all URLs in source file are still unchanged present.
        List<String> processedUrls = findUrls(processedContent);
        assertEquals(originalUrls, processedUrls, "URLs should be unchanged after preprocessing " + jsonFile);
    }

    @Test
    @DisplayName("Test removal of multi-line comments")
    void testMultiLineCommentRemoval() {
        String json5 = "{ \"key\": /* comment */ \"value\" }";
        String expectedJson = "{ \"key\":  \"value\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson.replaceAll("\\s", ""), actualJson.replaceAll("\\s", ""));
    }

    @Test
    @DisplayName("Test removal of single-line comments")
    void testSingleLineCommentRemoval() {
        String json5 = "{ \"key\": \"value\" // this is a comment\n }";
        String expectedJson = "{ \"key\": \"value\" \n }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson.replaceAll("\\s", ""), actualJson.replaceAll("\\s", ""));
    }

    @Test
    @DisplayName("Test conversion of single-quoted strings")
    void testSingleQuoteConversion() {
        String json5 = "{ 'key': 'value' }";
        String expectedJson = "{ \"key\": \"value\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    @DisplayName("Test removal of trailing comma in array")
    void testTrailingCommaInArray() {
        String json5 = "[ \"one\", \"two\", ]";
        String processed = Json5Preprocessor.toJson(json5);
        assertFalse(processed.contains(",]"));
    }

    @Test
    @DisplayName("Test removal of trailing comma in object")
    void testTrailingCommaInObject() {
        String json5 = "{ \"key\": \"value\", }";
        String processed = Json5Preprocessor.toJson(json5);
        assertFalse(processed.contains(",}"));
    }

    @Test
    @DisplayName("Test quoting of unquoted keys")
    void testUnquotedKeyQuoting() {
        String json5 = "{ key: \"value\" }";
        String expectedJson = "{ \"key\": \"value\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    @DisplayName("Test that URLs with backslashes are preserved")
    void testUrlWithBackslashes() {
        String json5 = "{ \"url\": \"https://example.com/path\\with\\backslashes\" }";
        String expectedJson = "{ \"url\": \"https://example.com/path\\with\\backslashes\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson.replaceAll("\\s", ""), actualJson.replaceAll("\\s", ""));
    }

    private List<String> findUrls(String content) {
        Matcher matcher = URL_PATTERN.matcher(content);
        return matcher.results().map(mr -> mr.group(0)).collect(Collectors.toList());
    }

}
