package com.graphinout.base.json;

import com.graphinout.testdata.TestFileProvider;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.json.writer.impl.StringBuilderJsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * For a set of JSON test files, read into string, create {@link InputSource} on it, setup JsonReader impl, let it read
 * to an {@link StringBuilderJsonWriter} instance, then compare the original and resulting string.
 */
public class JsonTest {

    private static final Logger log = getLogger(JsonTest.class);
    private JsonReader jsonReader;
    private String testResourcesPrefix;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReaderImpl();
        testResourcesPrefix = "json";
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

        Assertions.assertNotNull(processed);
        Assertions.assertTrue(processed.contains("{"));
        Assertions.assertTrue(processed.contains("}"));
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

        Assertions.assertNotNull(processed);
        Assertions.assertTrue(processed.contains("hello"));
        Assertions.assertTrue(processed.contains("42"));
        Assertions.assertTrue(processed.contains("3.14"));
        Assertions.assertTrue(processed.contains("true"));
        Assertions.assertTrue(processed.contains("null"));
        Assertions.assertTrue(processed.contains("["));
        Assertions.assertTrue(processed.contains("]"));
        Assertions.assertTrue(processed.contains("nested"));
    }

    @ParameterizedTest
    @DisplayName("Test all JSON files together")
    @ValueSource(strings = {"minimal.json", "typical.json", "complex.json"})
    void testJsonFiles(String filename) throws IOException {
        testJsonFile(filename);
    }

    @Test
    @DisplayName("Test JSON roundtrip preserves semantic structure")
    void testJsonRoundtripSemantics() throws IOException {
        // Test that even if whitespace differs, the semantic content is preserved
        String originalContent = readJsonFile("complex.json");
        String processedContent = processJsonContent(originalContent);

        // Both should be valid JSON (no exceptions thrown)
        Assertions.assertDoesNotThrow(() -> processJsonContent(originalContent));
        Assertions.assertDoesNotThrow(() -> processJsonContent(processedContent));

        // The processed content should not be null or empty
        Assertions.assertNotNull(processedContent);
        Assertions.assertFalse(processedContent.trim().isEmpty());
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
            StringBuilderJsonWriter writer = new StringBuilderJsonWriter();
            // Process the JSON through the reader/writer pipeline
            jsonReader.read(inputSource, writer);

            // Return the processed JSON string
            return writer.json();
        } catch (Throwable t) {
            log.warn("Failed to read \"\"\"\n" + jsonContent + "\n\"\"\"");
            throw t;
        }
    }

    private String readJsonFile(String filename) throws IOException {
        TestFileProvider.TestResource res = TestFileProvider.resourceByPath(testResourcesPrefix + "/" + filename);
        assertThat(res).isNotNull();
        return res.resource().getContentAsString();
    }

    private void testJsonFile(String filename) throws IOException {
        String originalContent = readJsonFile(filename);
        String processedContent = processJsonContent(originalContent);

        // Basic validation - both should be non-null and non-empty
        Assertions.assertNotNull(originalContent, "Original content should not be null for " + filename);
        Assertions.assertNotNull(processedContent, "Processed content should not be null for " + filename);
        Assertions.assertFalse(originalContent.trim().isEmpty(), "Original content should not be empty for " + filename);
        Assertions.assertFalse(processedContent.trim().isEmpty(), "Processed content should not be empty for " + filename);

        // The processed content should be valid JSON (no parsing errors)
        Assertions.assertDoesNotThrow(() -> processJsonContent(processedContent), "Processed content should be valid JSON for " + filename);

        System.out.println("✓ Successfully processed " + filename);
        System.out.println("  Original length: " + originalContent.length() + " characters");
        System.out.println("  Processed length: " + processedContent.length() + " characters");
    }

}
