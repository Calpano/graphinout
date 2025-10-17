package com.graphinout.foundation.json.value;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;

class JsonPathsTest {

    private static Predicate<Object> is(int i) {
        return o -> o.equals(i);
    }

    @Test
    void testEndsWith() {
        assertThat(JsonPaths.endsWith(List.of(1, 2, 3, 4), is(3), is(4))).isTrue();
        assertThat(JsonPaths.endsWith(List.of(1, 2, 3, 4), is(3), is(5))).isFalse();
        assertThat(JsonPaths.endsWith(List.of(1, 2, 3, 4), is(1), is(2))).isFalse();
        assertThat(JsonPaths.endsWith(List.of(1, 2, 3, 4), is(1), is(2), is(3), is(4))).isTrue();
    }

}
