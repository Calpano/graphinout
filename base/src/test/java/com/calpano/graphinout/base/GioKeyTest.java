package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioDefault;
import com.calpano.graphinout.base.gio.GioDescription;
import com.calpano.graphinout.base.gio.GioGraphInOutConstants;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GioKeyTest {
    @DisplayName("Test For generate XML tag from GioKey Object.")
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
            return Stream.of(new TestArgument(GioKey.builder()
                            .id("10")
                            .desc(GioDescription.builder().build())
                            .attrName("attr.name")
                            .attrType("attr.type")
                            .forType(GioKeyForType.All)
                            .extraAttrib("extra.attrib")
                            .defaultValue(GioDefault.builder().build()).build(),
                            "<key id=\"10\" attr.name=\"attr.name\" attr.type=\"attr.type\" for=\"All\" extra.attrib=\"extra.attrib\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "<desc/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR +
                                    "<default/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "</key>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioKey.builder()
                            .id("10")
                            .attrName("attr.name")
                            .attrType("attr.type")
                            .forType(GioKeyForType.All)
                            .extraAttrib("extra.attrib")
                            .defaultValue(GioDefault.builder().build()).build(),
                            "<key id=\"10\" attr.name=\"attr.name\" attr.type=\"attr.type\" for=\"All\" extra.attrib=\"extra.attrib\">" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "<default/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "</key>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR),
                    new TestArgument(GioKey.builder()
                            .id("10")
                            .attrName("attr.name")
                            .attrType("attr.type")
                            .forType(GioKeyForType.All)
                            .extraAttrib("extra.attrib").build(),
                            "<key id=\"10\" attr.name=\"attr.name\" attr.type=\"attr.type\" for=\"All\" extra.attrib=\"extra.attrib\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioKey.builder()
                            .id("10")
                            .attrName("attr.name")
                            .attrType("attr.type")
                            .forType(GioKeyForType.All).build(),
                            "<key id=\"10\" attr.name=\"attr.name\" attr.type=\"attr.type\" for=\"All\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioKey.builder()
                            .id("10")
                            .attrName("attr.name")
                            .attrType("attr.type").build(),
                            "<key id=\"10\" attr.name=\"attr.name\" attr.type=\"attr.type\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioKey.builder()
                            .id("10")
                            .attrName("attr.name").build(),
                            "<key id=\"10\" attr.name=\"attr.name\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioKey.builder()
                            .id("10").build(),
                            "<key id=\"10\"/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            ""),
                    new TestArgument(GioKey.builder().build(),
                            "<key/>" + GioGraphInOutConstants.NEW_LINE_SEPARATOR,
                            "",
                            "")
            );
        }
    }
}