package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.bridge.Java9;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.pure.collections.jajson.JaJson.createMap;

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
        String expected = "[ \"a very long value that will definitely exceed the max line length\",\n" +
                "  2\n" +
                "]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatComplexStructure() {
        String in = "{ \"name\": \"John Doe\",\n" +
                "  \"age\": 30,\n" +
                "  \"isStudent\": false,\n" +
                "  \"courses\":\n" +
                "  [ {\"title\": \"History\", \"credits\": 3},\n" +
                "    {\"title\": \"Math\", \"credits\": 4}\n" +
                "  ],\n" +
                "  \"address\":\n" +
                "  { \"street\": \"123 Main St\",\n" +
                "    \"city\": \"Anytown\"\n" +
                "  }\n" +
                "}\n";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 90);
        String expected = "{ \"name\": \"John Doe\",\n" +
                "  \"age\": 30,\n" +
                "  \"isStudent\": false,\n" +
                "  \"courses\": [ { \"title\": \"History\", \"credits\": 3 }, { \"title\": \"Math\", \"credits\": 4 } ],\n" +
                "  \"address\": { \"street\": \"123 Main St\", \"city\": \"Anytown\" }\n" +
                "}";
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
        String expected = "{ \"key1\":\n" +
                "    \"a very long value that will definitely exceed the max line length\",\n" +
                "  \"key2\": 2\n" +
                "}";
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
        String expected = "[ { \"key1\":\n" +
                "      \"a very long value that will definitely exceed the max line length\",\n" +
                "    \"key2\": 2\n" +
                "  }\n" +
                "]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatWithCustomLineLength() {
        String in = "{\"key1\": \"short\", \"key2\": \"another short\"}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 30);
        String expected = "{ \"key1\": \"short\",\n" +
                "  \"key2\": \"another short\"\n" +
                "}";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testObject() throws IOException {
        Map<String, Object> jaJsonInput = new TreeMap<>(Java9.Map.of("a", 2, "b", 3, "c", 4));
        String formatted = "{ \"a\": 2, \"b\": 3, \"c\": 4 }";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 120, Java9.Set.of());
        assertThat(actual).isEqualTo(formatted);
    }


    @Test
    void testObjectWithArrays_shortLine() {
        Map<String, Object> jaJsonInput = createMap() //
                .put("key1", Java9.List.of(1, 2, 3)) //
                .put("key2", Java9.List.of( //
                        createMap().put("nestedKey", "nestedValue").build(), //
                        createMap().put("anotherNestedKey", "anotherNestedValue").build()) //
                ).build();
        String formatted = "{ \"key1\": [ 1, 2, 3 ],\n" +
                "  \"key2\":\n" +
                "    [ { \"nestedKey\": \"nestedValue\" },\n" +
                "      { \"anotherNestedKey\":\n" +
                "          \"anotherNestedValue\"\n" +
                "      }\n" +
                "    ]\n" +
                "}";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 50, Java9.Set.of());
        assertThat(actual).isEqualTo(formatted);
    }

}


