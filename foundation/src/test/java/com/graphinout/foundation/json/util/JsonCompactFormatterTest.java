package com.graphinout.foundation.json.util;

import com.graphinout.foundation.jajson.JaJson;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class JsonCompactFormatterTest {

    @Test
    void formatEmptyObject() {
        String in = "{}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{}");
    }

    @Test
    void formatSimpleObject() {
        String in = "{\"key\": \"value\"}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{ \"key\": \"value\" }");
    }

    @Test
    void formatNestedObject() {
        String in = "{\"key\": {\"nestedKey\": \"nestedValue\"}}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{ \"key\": { \"nestedKey\": \"nestedValue\" } }");
    }

    @Test
    void formatSimpleArray() {
        String in = "[1, \"two\", false]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("[ 1, \"two\", false ]");
    }

    @Test
    void formatArrayOfObjects() {
        String in = "[{\"a\": 1}, {\"b\": 2}]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = "[ { \"a\": 1 }, { \"b\": 2 } ]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatObjectWithLongLine() {
        String in = "{\"key1\": \"a very long value that will definitely exceed the max line length\", \"key2\": 2}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = """
        { "key1": "a very long value that will definitely exceed the max line length",
          "key2": 2
        }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatArrayWithLongLine() {
        String in = "[\"a very long value that will definitely exceed the max line length\", 2]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = """
        [
          "a very long value that will definitely exceed the max line length",
          2
        ]""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatComplexStructure() {
        String in = """
                {
                  "name": "John Doe",
                  "age": 30,
                  "isStudent": false,
                  "courses": [
                    {"title": "History", "credits": 3},
                    {"title": "Math", "credits": 4}
                  ],
                  "address": {
                    "street": "123 Main St",
                    "city": "Anytown"
                  }
                }
                """;
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = """
                { "name": "John Doe",
                  "age": 30,
                  "isStudent": false,
                  "courses": [ { "title": "History", "credits": 3 }, { "title": "Math", "credits": 4 } ],
                  "address": { "street": "123 Main St", "city": "Anytown" }
                }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatWithCustomLineLength() {
        String in = "{\"key1\": \"short\", \"key2\": \"another short\"}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 40);
        String expected = """
        { "key1": "short",
          "key2": "another short"
        }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatWithForceMultiLineKeys() {
        String in = "{\"myList\": [1, 2, 3]}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 80, Set.of("myList"));
        String expected = """
        { "myList": [
            1,
            2,
            3
          ]
        }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatSingleElementArrayWithObject() {
        String in = "[{\"key1\": \"a very long value that will definitely exceed the max line length\", \"key2\": 2}]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = """
        [{
          "key1": "a very long value that will definitely exceed the max line length",
          "key2": 2
        }]""";
        assertThat(actual).isEqualTo(expected);
    }
}
