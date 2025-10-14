package com.calpano.graphinout.foundation.json.value;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class JsonTypesTest {

    @Test
    void test() {
        assertThat(JsonTypes.strictestType(12.34f)).isEqualTo(Float.class);
        assertThat(JsonTypes.strictestType(12.34d)).isEqualTo(Double.class);
        assertThat(JsonTypes.strictestType(12.345678901234567890123456789d)).isEqualTo(Double.class);
    }

}
