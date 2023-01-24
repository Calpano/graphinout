package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioDescription;
import com.calpano.graphinout.base.gio.GioGraphInOutConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioDescriptionTest {

    @DisplayName("Test For generate XML tag from GioDescription Object.")
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
            return Stream.of(new TestArgument(GioDescription.builder().value("test for value").build(), "<desc>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "test for value" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "</desc>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioDescription.builder().build(), "<desc/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR, "", ""));
        }
    }
}