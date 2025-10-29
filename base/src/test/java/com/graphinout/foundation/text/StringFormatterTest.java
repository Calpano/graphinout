package com.graphinout.foundation.text;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class StringFormatterTest {

    @Test
    void testLineBreaks() {
        String in = "aaa\nbbb\rccc\r\rddd\r\neee\n\rfff\n\nggg";
        String out = StringFormatter.normalizeLineBreaks(in);
        assertThat(out).isEqualTo("aaa\nbbb\nccc\n\nddd\neee\n\nfff\n\nggg");
    }

    @Test
    void testWrap() {
        String ten = "0123456789";
        String actual = StringFormatter.wrap(ten, 5);
        assertThat(actual).isEqualTo("01234\n56789");
    }


}
