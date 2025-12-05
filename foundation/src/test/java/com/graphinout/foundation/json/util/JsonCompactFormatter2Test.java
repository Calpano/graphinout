package com.graphinout.foundation.json.util;

import com.graphinout.foundation.jajson.JaJson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.jajson.JaJson.createMap;

class JsonCompactFormatter2Test {


    @Test
    void formatWithForceMultiLineKeys() {
        String in = "{\"myList\": [1, 2, 3]}";
        String actual = JsonCompactFormatter.formatCompact(JaJson.parse(in), 60, Set.of("myList"));
        String expected = """
                { "myList":
                    [ 1,
                      2,
                      3
                    ]
                }""";
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void testObjectWithArrays_forced() {
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
                      { "anotherNestedKey": "anotherNestedValue" }
                    ]
                }""";
        String actual = JsonCompactFormatter.formatCompact(jaJsonInput, 80, Set.of("key2"));
        assertThat(actual).isEqualTo(formatted);
    }


}


