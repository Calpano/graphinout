package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.bridge.Java9;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.pure.collections.jajson.JaJson.createMap;

class JsonCompactFormatter2Test {


    @Test
    void formatWithForceMultiLineKeys() {
        String in = "{\"myList\": [1, 2, 3]}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 60, Java9.Set.of("myList"));
        String expected = "{ \"myList\":\n" +
                "    [ 1,\n" +
                "      2,\n" +
                "      3\n" +
                "    ]\n" +
                "}";
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void testObjectWithArrays_forced() {
        Map<String, Object> jaJsonInput = createMap() //
                .put("key1", Java9.List.of(1, 2, 3)) //
                .put("key2", Java9.List.of( //
                        createMap().put("nestedKey", "nestedValue").build(), //
                        createMap().put("anotherNestedKey", "anotherNestedValue").build()) //
                ).build();
        String formatted = "{ \"key1\": [ 1, 2, 3 ],\n" +
                "  \"key2\":\n" +
                "    [ { \"nestedKey\": \"nestedValue\" },\n" +
                "      { \"anotherNestedKey\": \"anotherNestedValue\" }\n" +
                "    ]\n" +
                "}";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 80, Java9.Set.of("key2"));
        assertThat(actual).isEqualTo(formatted);
    }


}


