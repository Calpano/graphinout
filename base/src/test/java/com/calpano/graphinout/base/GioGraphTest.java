package com.calpano.graphinout.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioGraphTest {
    @DisplayName("Test For generate XML tag from GioGraph Object. this test has problem with  writer strategy.")
    @Nested
    class successfulTest {

        @ParameterizedTest()
        @MethodSource("testArgumentStream")
        @Disabled
        void xmlValueTest(TestArgument testArgument) {
            assertEquals(testArgument.getExpectedStartTag(), testArgument.getActual().startTag());
            assertEquals(testArgument.getExpectedValueTag(), testArgument.getActual().valueTag());
            assertEquals(testArgument.getExpectedEndTag(), testArgument.getActual().endTag());
            assertEquals(testArgument.getExpectedFullTag(), testArgument.getActual().fullTag());
        }

        private static Stream<TestArgument> testArgumentStream() {

            return Stream.of(new TestArgument(GioGraph.builder()
                            .id("10")
                            .desc(GioDescription.builder().build())
                            .node(GioNode.builder().id("test Id").build())
                            .build(),
                            "<endpoint id=\"10\" node=\"node\" port=\"port\" type=\"In\" >" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "<desc/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "</endpoint>"+GioGraphInOutConstants.NEW_LINE_SEPARATOR)
            );
        }

    }
}