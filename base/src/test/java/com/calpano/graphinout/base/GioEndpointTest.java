package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioDescription;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraphInOutConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioEndpointTest {
    @DisplayName("Test For generate XML tag from GioEndpoint Object.")
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
            return Stream.of(new TestArgument(GioEndpoint.builder()
                            .id("10")
                            .desc(GioDescription.builder().build())
                            .port("port")
                            .node("node").type(Direction.In).build(),
                            "<endpoint id=\"10\" node=\"node\" port=\"port\" type=\"In\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "<desc/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "</endpoint>"+GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioEndpoint.builder()
                            .id("10")
                            .port("port")
                            .node("node").type(Direction.In).build(),
                            "<endpoint id=\"10\" node=\"node\" port=\"port\" type=\"In\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioEndpoint.builder()
                            .id("10")
                            .port("port")
                            .node("node").build(),
                            "<endpoint id=\"10\" node=\"node\" port=\"port\" type=\"Undirected\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioEndpoint.builder()
                            .id("10")
                            .port("port")
                            .build(),
                            "<endpoint id=\"10\" port=\"port\" type=\"Undirected\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioEndpoint.builder()
                            .id("10")
                            .build(),
                            "<endpoint id=\"10\" type=\"Undirected\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioEndpoint.builder()
                            .build(),
                            "<endpoint type=\"Undirected\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "")



                    );
        }
    }
}