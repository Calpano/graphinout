package com.calpano.graphinout.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioLocatorTest {
    @DisplayName("Test For generate XML tag from GioLocator Object.")
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

        private static Stream<TestArgument> testArgumentStream() throws MalformedURLException {
            return Stream.of(new TestArgument(GioLocator.builder()
                            .xLinkHref(new URL("http://127.0.0.1"))
                            .xLinkType("xlink.type")
                            .locatorExtraAttrib("locator.extra.attrib").build(),
                            "<locator xlink:herf=\"http://127.0.0.1\" xlink:type=\"xlink.type\" locator.extra.attrib=\"locator.extra.attrib\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "</locator>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioLocator.builder()
                            .xLinkHref(new URL("http://127.0.0.1"))
                            .xLinkType("xlink.type").build(),

                            "<locator xlink:herf=\"http://127.0.0.1\" xlink:type=\"xlink.type\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "</locator>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioLocator.builder()
                            .xLinkHref(new URL("http://127.0.0.1")).build(),
                            "<locator xlink:herf=\"http://127.0.0.1\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "</locator>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioLocator.builder().build(),
                            "<locator>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "</locator>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR)

                    );
        }
    }
}