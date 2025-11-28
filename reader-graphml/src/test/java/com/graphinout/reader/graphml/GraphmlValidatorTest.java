package com.graphinout.reader.graphml;

import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.reader.graphml.validation.GraphmlValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphmlValidatorTest {

    static String goodGraphml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
              <graph id="G" edgedefault="undirected">
                <node id="n0"/>
                <node id="n1"/>
                <node id="n2"/>
                <edge id="e0" source="n0" target="n2"/>
                <edge id="e1" source="n0" target="n1"/>
              </graph>
            </graphml>""";

    static String badGraphml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
              <graph id="G" edgedefault="undirected">
                <node id="n0"/>
                <node id="n1"/>
                <funnybunny/>
                <node id="n2"/>
                <edge id="e0" source="n0" target="n2"/>
                <edge id="e1" source="n0" target="n1"/>
              </graph>
            </graphml>""";

    @Test
    void isValidGraphml() {
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGood() throws IOException {
        assertTrue(GraphmlValidator.isValidGraphml(SingleInputSource.of("good", goodGraphml)));
    }

    @Test
    void testWrongElement() throws IOException {
        assertFalse(GraphmlValidator.isValidGraphml(SingleInputSource.of("funnybunny", badGraphml)));
    }

}
