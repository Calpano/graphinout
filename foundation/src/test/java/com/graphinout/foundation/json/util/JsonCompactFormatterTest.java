package com.graphinout.foundation.json.util;

import com.graphinout.foundation.jajson.JaJson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.jajson.JaJson.createMap;

class JsonCompactFormatterTest {

    @Test
    void formatArrayOfObjects() {
        String in = "[{\"a\": 1}, {\"b\": 2}]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        String expected = "[ { \"a\": 1 }, { \"b\": 2 } ]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatArrayWithLongLine() {
        String in = "[\"a very long value that will definitely exceed the max line length\", 2]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 60);
        String expected = """
                [ "a very long value that will definitely exceed the max line length",
                  2
                ]""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatComplexStructure() {
        String in = """
                { "name": "John Doe",
                  "age": 30,
                  "isStudent": false,
                  "courses":
                  [ {"title": "History", "credits": 3},
                    {"title": "Math", "credits": 4}
                  ],
                  "address":
                  { "street": "123 Main St",
                    "city": "Anytown"
                  }
                }
                """;
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 90);
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
    void formatEmptyObject() {
        String in = "{}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{}");
    }

    @Test
    void formatNestedObject() {
        String in = "{\"key\": {\"nestedKey\": \"nestedValue\"}}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{ \"key\": { \"nestedKey\": \"nestedValue\" } }");
    }

    @Test
    void formatObjectWithLongLine() {
        String in = "{\"key1\": \"a very long value that will definitely exceed the max line length\", \"key2\": 2}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 60);
        String expected = """
                { "key1":
                    "a very long value that will definitely exceed the max line length",
                  "key2": 2
                }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatSimpleArray() {
        String in = "[1, \"two\", false]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("[ 1, \"two\", false ]");
    }

    @Test
    void formatSimpleObject() {
        String in = "{\"key\": \"value\"}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in));
        assertThat(actual).isEqualTo("{ \"key\": \"value\" }");
    }

    @Test
    void formatSingleElementArrayWithObject() {
        String in = "[{\"key1\": \"a very long value that will definitely exceed the max line length\", \"key2\": 2}]";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 50);
        String expected = """
                [ { "key1":
                      "a very long value that will definitely exceed the max line length",
                    "key2": 2
                  }
                ]""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatWithCustomLineLength() {
        String in = "{\"key1\": \"short\", \"key2\": \"another short\"}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 30);
        String expected = """
                { "key1": "short",
                  "key2": "another short"
                }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Disabled
    void formatWithForceMultiLineKeys() {
        String in = "{\"myList\": [1, 2, 3]}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 80, Set.of("myList"));
        String expected = """
                {
                  "myList": [
                    1,
                    2,
                    3
                  ]
                }""";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testObject() throws IOException {
        Map<String, Object> jaJsonInput = new TreeMap<>(Map.of("a", 2, "b", 3, "c", 4));
        String formatted = """
                { "a": 2, "b": 3, "c": 4 }""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 120, Set.of());
        assertThat(actual).isEqualTo(formatted);
    }

    @Test
    @Disabled
    void testObjectWithArrays_forced() {
        Map<String, Object> jaJsonInput = createMap() //
                .put("key1", List.of(1, 2, 3)) //
                .put("key2", List.of( //
                        createMap().put("nestedKey", "nestedValue").build(), //
                        createMap().put("anotherNestedKey", "anotherNestedValue").build()) //
                ).build();
        String formatted = """
                {
                  "key1": [ 1, 2, 3 ],
                  "key2": [
                    { "nestedKey": "nestedValue" },
                    { "anotherNestedKey": "anotherNestedValue" }
                  ]
                }""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 80, Set.of("key1", "key2"));
        assertThat(actual).isEqualTo(formatted);
    }

    @Test
    void testObjectWithArrays_shortLine() {
        Map<String, Object> jaJsonInput = createMap() //
                .put("key1", List.of(1, 2, 3)) //
                .put("key2", List.of( //
                        createMap().put("nestedKey", "nestedValue").build(), //
                        createMap().put("anotherNestedKey", "anotherNestedValue").build()) //
                ).build();
        String formatted = """
                { "key1": [ 1, 2, 3 ],
                  "key2":
                    [ { "nestedKey": "nestedValue" },
                      { "anotherNestedKey":
                          "anotherNestedValue"
                      }
                    ]
                }""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 50, Set.of());
        assertThat(actual).isEqualTo(formatted);
    }

}


