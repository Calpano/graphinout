package com.graphinout.foundation.util;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextTestsTest {

    @Test
    void test() {
        assertThat(TextTests.xAssertEqual("aaa", "aaa")).isTrue();
        assertThrows(AssertionError.class, () -> TextTests.xAssertEqual("aaa", "bbb"));
        //TextTests.xAssertEqual("xxxaaa", "xxxbbb");
    }

    @Test
    void testHighlight() {
        String s = TextTests.toHighlight("x0123456789a0123456789x", 11);
        assertThat(s).isEqualTo("...012345678 '9'<57> __'a'<97>__ '0'<48> 123456789...");
    }

}
