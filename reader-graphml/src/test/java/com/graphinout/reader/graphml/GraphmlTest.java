package com.graphinout.reader.graphml;

import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.xml.writer.Xml2StringWriter;
import com.graphinout.foundation.xml.util.XmlTool;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("Test XML<->Graphml (all)")
    void testAllGraphmlFiles(String displayPath, Resource xmlResource) throws Exception {
        if (TestFileUtil.isInvalid(xmlResource, "graphml", "xml")) {
            log.debug("Skipping invalid Graphml file: {}", xmlResource.getURI());
            return;
        }

        // == pre-flight check
        XmlTool.assertCanParseAsXml(xmlResource);

        // == actual test
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);

        XmlTool.parseAndWriteXml(xmlResource, xml2graphml);

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

