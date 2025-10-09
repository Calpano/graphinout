package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.DelegatingXmlWriter;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter.AttributeOrderPerElement;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import io.github.classgraph.Resource;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("Test XML<->Graphml (all)")
    void testAllGraphmlFiles(String displayPath, Resource xmlResource) throws Exception {
        if(TestFileUtil.isInvalid(xmlResource, "graphml","xml")) {
            log.debug("Skipping invalid Graphml file: {}", xmlResource.getURI());
            return;
        }

        // == pre-flight check
        File xmlFile = TestFileUtil.file(xmlResource);
        assert xmlFile != null;
        Path xmlFilePath = xmlFile.toPath();
        XmlTool.assertCanParseAsXml(xmlFilePath);

        // == actual test
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);

        XmlTool.parseAndWriteXml(xmlFile, xml2graphml);

        String xml_in = xmlResource.getContentAsString();
        String xml_out = xmlWriter.resultString();

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in, ()->{
            try {
                log.info("== Input XML\n{}",xmlResource.getContentAsString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

