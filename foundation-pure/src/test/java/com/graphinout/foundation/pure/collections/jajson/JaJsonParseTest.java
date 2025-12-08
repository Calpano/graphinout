package com.graphinout.foundation.pure.collections.jajson;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JaJsonParseTest {

    @Test
    void parse_primitives_and_null() {
        assertThat(JaJson.parse("null")).isEqualTo(null);
        assertThat(JaJson.parse("true")).isEqualTo(true);
        assertThat(JaJson.parse("false")).isEqualTo(false);
        assertThat(JaJson.parse("0")).isEqualTo(0);
        assertThat(JaJson.parse("42")).isEqualTo(42);
        assertThat(JaJson.parse("-1")).isEqualTo(-1);
        assertThat(JaJson.parse("3.5")).isEqualTo(3.5);
        assertThat(JaJson.parse("1e3")).isEqualTo(1000.0);
    }

    @Test
    void parse_large_numbers_and_precision() {
        // Larger than Long.MAX_VALUE should become BigDecimal
        Object big = JaJson.parse("9223372036854775808");
        assertThat(big).isInstanceOf(BigDecimal.class);
        assertThat(((BigDecimal) big).toString()).isEqualTo("9223372036854775808");

        // Large integer with many digits
        Object veryBig = JaJson.parse("123456789012345678901234567890");
        assertThat(veryBig).isInstanceOf(BigDecimal.class);
        assertThat(((BigDecimal) veryBig).toString()).isEqualTo("123456789012345678901234567890");
    }

    @Test
    void parse_strings_and_escapes() {
        assertThat(JaJson.parse("\"hello\""))
                .isEqualTo("hello");
        assertThat(JaJson.parse("\"\\\"quote\\\" \\\\ backslash\""))
                .isEqualTo("\"quote\" \\ backslash");
        assertThat(JaJson.parse("\"line\\nbreak\\tand\\rcarriage\\fform\\bback\""))
                .isEqualTo("line\nbreak\tand\rcarriage\fform\bback");
        assertThat(JaJson.parse("\"A\\u0001B\""))
                .isEqualTo("A\u0001B");
    }

    @Test
    void parse_arrays_and_objects() {
        Object arr = JaJson.parse("[\n  \"x\", null , 7 , [1,2], {\"a\":1}\n]");
        assertThat(arr).isInstanceOf(List.class);
        List<?> list = (List<?>) arr;
        assertThat(list.get(0)).isEqualTo("x");
        assertThat(list.get(1)).isEqualTo(null);
        assertThat(list.get(2)).isEqualTo(7);
        assertThat(list.get(3)).isInstanceOf(List.class);
        assertThat(((List<?>) list.get(3))).containsExactly(1,2).inOrder();
        assertThat(list.get(4)).isInstanceOf(Map.class);
        assertThat(((Map<?,?>) list.get(4)).get("a")).isEqualTo(1);

        Object obj = JaJson.parse("{\"a\":1,\"b\":[\"z\",3],\"c\":{}} ");
        Map<?,?> map = (Map<?,?>) obj;
        assertThat(map.get("a")).isEqualTo(1);
        assertThat(map.get("b")).isInstanceOf(List.class);
        assertThat(((List<?>) map.get("b"))).containsExactly("z", 3).inOrder();
        assertThat(map.get("c")).isInstanceOf(Map.class);
        assertThat(((Map<?,?>) map.get("c")).isEmpty()).isTrue();
    }

    @Test
    void parse_errors() {
        assertThrows(IllegalArgumentException.class, () -> JaJson.parse("[1,2"));
        assertThrows(IllegalArgumentException.class, () -> JaJson.parse("{\"a\" 1}"));
        assertThrows(IllegalArgumentException.class, () -> JaJson.parse("\"unterminated"));
        assertThrows(IllegalArgumentException.class, () -> JaJson.parse("tru"));
    }
}
