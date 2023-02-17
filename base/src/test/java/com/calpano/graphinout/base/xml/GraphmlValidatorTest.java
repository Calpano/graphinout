package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphmlValidatorTest {

    static String goodGraphml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" + //
            "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
            "      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" + //
            "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" + //
            "  <graph id=\"G\" edgedefault=\"undirected\">\n" + //
            "    <node id=\"n0\"/>\n" + //
            "    <node id=\"n1\"/>\n" + //
            "    <node id=\"n2\"/>\n" + //
            "    <edge id=\"e0\" source=\"n0\" target=\"n2\"/>\n" + //
            "    <edge id=\"e1\" source=\"n0\" target=\"n1\"/>\n" + //
            "  </graph>\n" + //
            "</graphml>";

    static String badGraphml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" + //
            "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
            "      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" + //
            "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" + //
            "  <graph id=\"G\" edgedefault=\"undirected\">\n" + //
            "    <node id=\"n0\"/>\n" + //
            "    <node id=\"n1\"/>\n" + //

            "    <funnybunny/>\n" + //

            "    <node id=\"n2\"/>\n" + //
            "    <edge id=\"e0\" source=\"n0\" target=\"n2\"/>\n" + //
            "    <edge id=\"e1\" source=\"n0\" target=\"n1\"/>\n" + //
            "  </graph>\n" + //
            "</graphml>";

    @Test
    void testGood() throws IOException {
        assertTrue(GraphmlValidator.isValidGraphml(SingleInputSource.of("good", goodGraphml)));
    }

    @Test
    void testWrongElement() throws IOException {
        assertFalse(GraphmlValidator.isValidGraphml(SingleInputSource.of("funnybunny", badGraphml)));
    }

}