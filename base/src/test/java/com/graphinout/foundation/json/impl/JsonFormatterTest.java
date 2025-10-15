package com.graphinout.foundation.json.impl;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class JsonFormatterTest {


    /** test {@link JsonFormatter#formatDebug(String)} on a small synthetic sample */
    @Test
    void test() {
        String json = """
                {
                  "name": "John Doe",
                  "age": 30,
                  "isStudent": false,
                  "courses": [
                    {"title": "History I", "credits": 3},
                    {"title": "Math II", "credits": 4}
                  ],
                  "address": null
                }""";

        String expected = """
                {"name":
                "John Doe","age":
                30,"isStudent":
                false,"courses":
                [{"title":
                "History I","credits":
                3},{"title":
                "Math II","credits":
                4}],"address":
                null}""";

        String formattedJson = JsonFormatter.formatDebug(json);
        assertThat(formattedJson).isEqualTo(expected);
    }

}
