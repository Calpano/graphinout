package com.calpano.graphinout.foundation;

import com.calpano.graphinout.foundation.text.StringFormatter;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class StringFormatterTest {

    @Test
    void test() {
        String ten = "0123456789";
        String actual = StringFormatter.wrap(ten, 5);
        assertThat(actual).isEqualTo("01234\n56789");
    }


}
