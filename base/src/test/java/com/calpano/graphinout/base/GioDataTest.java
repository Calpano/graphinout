package com.calpano.graphinout.base;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioDataTest {

    @DisplayName("Test For generate XML tag from GioData Object.")
    @Nested
    class successfulTest {

        @ParameterizedTest()
        @MethodSource("testArgumentStream")
        void xmlValueTest(TestArgument testArgument) {
            assertEquals(testArgument.getExpectedStartTag(), testArgument.getActual().startTag());
            assertEquals(testArgument.getExpectedValueTag(), testArgument.getActual().valueTag());
            assertEquals(testArgument.getExpectedEndTag(), testArgument.getActual().endTag());
            assertEquals(testArgument.getExpectedFullTag(), testArgument.getActual().fullTag());
        }

        private static Stream<TestArgument> testArgumentStream() {
            return Stream.of(new TestArgument(GioData.builder().id("test").key("10").value("50").build(), "<data id=\"test\" key=\"10\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "50" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "</data>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioData.builder().id("test").value("50").build(), "<data id=\"test\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "50" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "</data>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioData.builder().id("test").key("10").build(), "<data id=\"test\" key=\"10\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "", ""),
                    new TestArgument(GioData.builder().id("test").build(), "<data id=\"test\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "", ""));
        }

    }
}