package com.graphinout.foundation.pure.json.json5;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import com.graphinout.testdata.TestFileProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Json5PreprocessorTest {

    @ParameterizedTest
    @MethodSource("com.graphinout.testdata.TestFileProvider#json5InputSources")
    void testJson5(String name, TestFileProvider.NamedString input) throws IOException {
        testInput(input);
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
    @DisplayName("String values that look like unquoted keys must not be rewritten")
    void testKeyLikeContentInsideStringIsPreserved() {
        // A record-style label whose value contains "{input:|output:}". The key-quoting must NOT touch it.
        String json5 = "{ \"value\": \"priority: Dense|{input:|output:}|{{(?, 172)}}\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(json5, actualJson);
    }

    @Test
    @DisplayName("Trailing-comma-like content inside a string must not be removed")
    void testTrailingCommaInsideStringIsPreserved() {
        String json5 = "{ \"value\": \"a,]b,}c\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(json5, actualJson);
    }

    @Test
    @DisplayName("Test that URLs with backslashes are preserved")
    void testUrlWithBackslashes() {
        String json5 = "{ \"url\": \"https://example.com/path\\with\\backslashes\" }";
        String expectedJson = "{ \"url\": \"https://example.com/path\\with\\backslashes\" }";
        String actualJson = Json5Preprocessor.toJson(json5);
        assertEquals(expectedJson.replaceAll("\\s", ""), actualJson.replaceAll("\\s", ""));
    }

    private void testInput(TestFileProvider.NamedString input) throws IOException {
        String json = Json5Preprocessor.toJson(input.content());
        // Validate json is valid JSON syntax by parsing it
        tools.jackson.databind.json.JsonMapper objectMapper = tools.jackson.databind.json.JsonMapper.builder()
                .enable(tools.jackson.core.StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                // enable all json5 features
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS)
                .enable(tools.jackson.core.json.JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
                .build();
        // TODO allow hex numbers, see https://github.com/FasterXML/jackson-core/issues/707

        objectMapper.readTree(json); // This will throw JacksonException if JSON is invalid
    }

}
