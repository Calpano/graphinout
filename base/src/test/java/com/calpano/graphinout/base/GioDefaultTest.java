package com.calpano.graphinout.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioDefaultTest {

    @DisplayName("Test For generate XML tag from GioDefault Object.")
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
            return Stream.of(new TestArgument(GioDefault.builder().value("test for value").defaultType("IN").build(), "<default default.type=\"IN\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "test for value" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "</default>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioDefault.builder().value("test for value").build(), "<default>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "test for value" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "</default>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioDefault.builder().defaultType("IN").build(), "<default default.type=\"IN\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "", ""),
                    new TestArgument(GioDefault.builder().build(), "<default/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "", ""));
        }
    }
}