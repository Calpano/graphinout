package com.graphinout.reader.graphml;

import com.graphinout.base.xml.util.XmlTestTool;
import com.graphinout.testdata.TestFileUtil;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.base.xml.util.XmlTool;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#graphmlResources")
    @DisplayName("Test XML<->Graphml (all)")
    void testAllGraphmlFiles(String displayPath, Resource xmlResource) throws Exception {
        // Deliberately-malformed fixtures cannot round-trip, so this test does not apply to them. It used
        // to `return`, which reported a PASS having asserted nothing and left no trace in the report; an
        // abort records a real skip with a reason instead. No coverage is lost: that these fixtures are
        // REJECTED is asserted in GraphmlReaderContentErrorTest#readAllGraphmlFiles.
        Assumptions.assumeFalse(TestFileUtil.isInvalid(xmlResource, "graphml", "xml"),
                () -> "fixture is tagged --INVALID; rejection is asserted in GraphmlReaderContentErrorTest: "
                        + xmlResource.getPath());

        // == pre-flight check
        XmlTestTool.assertCanParseAsXml(xmlResource);

        // == actual test
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);

        XmlTestTool.parseAndWriteXml(xmlResource, xml2graphml);

        String xml_in = xmlResource.getContentAsString();
        String xml_out = xmlWriter.resultString();

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in, () -> {
            try {
                log.info("== Input XML\n{}", xmlResource.getContentAsString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

