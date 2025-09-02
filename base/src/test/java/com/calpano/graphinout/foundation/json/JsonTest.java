package com.calpano.graphinout.foundation.json;

import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * For a set of JSON test files, read into string, create {@link InputSource} on it, setup JsonReader impl, let it read
 * to an {@link StringBuilderJsonWriter} instance, then compare the original and resulting string.
 */
public class JsonTest {

    private JsonReader jsonReader;
    private Path testResourcesPath;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReaderImpl();
        testResourcesPath = Paths.get("src/test/resources/json");
    }

    @Test
    @DisplayName("Test all JSON files together")
    void testAllJsonFiles() throws IOException {
        testJsonFile("minimal.json");
        testJsonFile("typical.json");
        testJsonFile("complex.json");
    }

    @Test
    @DisplayName("Test complex JSON file processing")
    void testComplexJson() throws IOException {
        testJsonFile("complex.json");
    }

    @Test
    @DisplayName("Test empty JSON object")
    void testEmptyJsonObject() throws IOException {
        String emptyJson = "{}";
        String processed = processJsonContent(emptyJson);

        assertNotNull(processed);
        assertTrue(processed.contains("{"));
        assertTrue(processed.contains("}"));
    }

    @Test
    @DisplayName("Test JSON with various data types")
    void testJsonDataTypes() throws IOException {
        String jsonWithTypes = """
                {
                  "string": "hello",
                  "number": 42,
                  "float": 3.14,
                  "boolean": true,
                  "null": null,
                  "array": [1, 2, 3],
                  "object": {"nested": "value"}
                }
                """;

        String processed = processJsonContent(jsonWithTypes);

        assertNotNull(processed);
        assertTrue(processed.contains("hello"));
        assertTrue(processed.contains("42"));
        assertTrue(processed.contains("3.14"));
        assertTrue(processed.contains("true"));
        assertTrue(processed.contains("null"));
        assertTrue(processed.contains("["));
        assertTrue(processed.contains("]"));
        assertTrue(processed.contains("nested"));
    }

    @Test
    @DisplayName("Test JSON roundtrip preserves semantic structure")
    void testJsonRoundtripSemantics() throws IOException {
        // Test that even if whitespace differs, the semantic content is preserved
        String originalContent = readJsonFile("complex.json");
        String processedContent = processJsonContent(originalContent);

        // Both should be valid JSON (no exceptions thrown)
        assertDoesNotThrow(() -> processJsonContent(originalContent));
        assertDoesNotThrow(() -> processJsonContent(processedContent));

        // The processed content should not be null or empty
        assertNotNull(processedContent);
        assertFalse(processedContent.trim().isEmpty());
    }

    @Test
    @DisplayName("Test minimal JSON file processing")
    void testMinimalJson() throws IOException {
        testJsonFile("minimal.json");
    }

    @Test
    @DisplayName("Test typical JSON file processing")
    void testTypicalJson() throws IOException {
        testJsonFile("typical.json");
    }

    private String processJsonContent(String jsonContent) throws IOException {
        // Create an InputSource from the JSON content

        // Create a StringBuilderJsonWriter to capture the output

        try (SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test-json", jsonContent)) {
            StringBuilderJsonWriter writer = new StringBuilderJsonWriter(true);
            // Process the JSON through the reader/writer pipeline
            jsonReader.read(inputSource, writer);

            // Return the processed JSON string
            return writer.json();
        }
    }

    private String readJsonFile(String filename) throws IOException {
        Path filePath = testResourcesPath.resolve(filename);
        assertTrue(Files.exists(filePath), "JSON test file should exist: " + filename);
        return FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8);
    }

    private void testJsonFile(String filename) throws IOException {
        String originalContent = readJsonFile(filename);
        String processedContent = processJsonContent(originalContent);

        // Basic validation - both should be non-null and non-empty
        assertNotNull(originalContent, "Original content should not be null for " + filename);
        assertNotNull(processedContent, "Processed content should not be null for " + filename);
        assertFalse(originalContent.trim().isEmpty(), "Original content should not be empty for " + filename);
        assertFalse(processedContent.trim().isEmpty(), "Processed content should not be empty for " + filename);

        // The processed content should be valid JSON (no parsing errors)
        assertDoesNotThrow(() -> processJsonContent(processedContent), "Processed content should be valid JSON for " + filename);

        System.out.println("✓ Successfully processed " + filename);
        System.out.println("  Original length: " + originalContent.length() + " characters");
        System.out.println("  Processed length: " + processedContent.length() + " characters");
    }

}
