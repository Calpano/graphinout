package com.graphinout.foundation.pure.text;

import com.graphinout.foundation.pure.log.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.slf4j.LoggerFactory.getLogger;

class TextTestsTest {

    private static final Logger log = getLogger(TextTestsTest.class);

    static TextTestTool TTT =  TextTestTool.of(LoggerFactory.getLogger(TextTestsTest.class));

    @Test
    void test() {
        assertThat(TTT.xAssertEqual("aaa", "aaa")).isTrue();
        assertThrows(AssertionError.class, () -> TTT.xAssertEqual("aaa", "bbb"));
        //TextTests.xAssertEqual("xxxaaa", "xxxbbb");
    }

    @Test
    void testHighlight() {
        String s = TextTestTool.toHighlight("x0123456789a0123456789x", 11);
        assertThat(s).isEqualTo("...012345678 '9'<57> __'a'<97>__ '0'<48> 123456789...");
    }

}
