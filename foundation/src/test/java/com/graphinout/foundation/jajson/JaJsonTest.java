package com.graphinout.foundation.jajson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JaJsonTest {

    private enum SampleEnum { ALPHA, BETA }

    @Test
    void primitives_and_null() {
        assertThat(JaJson.toJsonString(null)).isEqualTo("null");
        assertThat(JaJson.toJsonString(true)).isEqualTo("true");
        assertThat(JaJson.toJsonString(false)).isEqualTo("false");
        assertThat(JaJson.toJsonString(0)).isEqualTo("0");
        assertThat(JaJson.toJsonString(42L)).isEqualTo("42");
        assertThat(JaJson.toJsonString(3.5)).isEqualTo("3.5");
    }

    @Test
    void reject_nan_and_infinity() {
        assertThrows(IllegalArgumentException.class, () -> JaJson.toJsonString(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> JaJson.toJsonString(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, JaJsonTest::toJsonFloat);
    }

    private static String toJsonFloat() {
        // Helper to use float constants without implicit double boxing
        return JaJson.toJsonString(Float.NEGATIVE_INFINITY);
    }

    @Test
    void strings_and_characters_are_escaped() {
        assertThat(JaJson.toJsonString("hello"))
                .isEqualTo("\"hello\"");
        assertThat(JaJson.toJsonString("\"quote\" \\ backslash"))
                .isEqualTo("\"\\\"quote\\\" \\\\ backslash\"");
        assertThat(JaJson.toJsonString("line\nbreak\tand\rcarriage\fform\bback"))
                .isEqualTo("\"line\\nbreak\\tand\\rcarriage\\fform\\bback\"");
        // Control char < 0x20 should be rendered as \\u00XX
        String withCtrl = "A" + (char)1 + "B";
        assertThat(JaJson.toJsonString(withCtrl))
                .isEqualTo("\"A\\u0001B\"");

        // Characters become 1-length strings
        assertThat(JaJson.toJsonString('x')).isEqualTo("\"x\"");
    }

    @Test
    void arrays_and_lists() {
        int[] ints = new int[] {1, 2, 3};
        assertThat(JaJson.toJsonString(ints)).isEqualTo("[1,2,3]");

        char[] chars = new char[] {'a','b'};
        // char elements are serialized as JSON strings
        assertThat(JaJson.toJsonString(chars)).isEqualTo("[\"a\",\"b\"]");

        List<Object> list = new ArrayList<>();
        list.add("x");
        list.add(null);
        list.add(7);
        assertThat(JaJson.toJsonString(list)).isEqualTo("[\"x\",null,7]");
    }

    @Test
    void maps_and_nested_structures() {
        Map<Object, Object> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put(2, "b"); // non-string key becomes String via String.valueOf
        m.put(null, true); // null key is rendered as "null"
        List<Object> nested = new ArrayList<>();
        nested.add("z");
        nested.add(3);
        m.put("list", nested);

        String json = JaJson.toJsonString(m);
        assertThat(json).isEqualTo("{\"a\":1,\"2\":\"b\",\"null\":true,\"list\":[\"z\",3]}");
    }

    @Test
    void enums_and_unsupported_types() {
        assertThat(JaJson.toJsonString(SampleEnum.BETA)).isEqualTo("\"BETA\"");
        assertThrows(IllegalArgumentException.class, () -> JaJson.toJsonString(new Object()));
    }
}
