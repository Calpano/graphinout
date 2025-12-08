package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.text.JsonFormatting;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class JsonFormatterTest {


    /** test {@link JsonFormatting#formatDebug(String)} on a small synthetic sample */
    @Test
    void test() {
        String json = "{\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"age\": 30,\n" +
                "  \"isStudent\": false,\n" +
                "  \"courses\": [\n" +
                "    {\"title\": \"History I\", \"credits\": 3},\n" +
                "    {\"title\": \"Math II\", \"credits\": 4}\n" +
                "  ],\n" +
                "  \"address\": null\n" +
                "}";

        String expected = "{\"name\":\n" +
                "\"John Doe\",\"age\":\n" +
                "30,\"isStudent\":\n" +
                "false,\"courses\":\n" +
                "[{\"title\":\n" +
                "\"History I\",\"credits\":\n" +
                "3},{\"title\":\n" +
                "\"Math II\",\"credits\":\n" +
                "4}],\"address\":\n" +
                "null}";

        String formattedJson = JsonFormatting.formatDebug(json);
        assertThat(formattedJson).isEqualTo(expected);
    }

}
