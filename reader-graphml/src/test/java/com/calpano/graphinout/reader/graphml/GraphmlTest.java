package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("Test all GraphML: XML -> GraphML -> XML")
    void testAllGraphmlFiles(String displayPath, Resource xmlResource) throws Exception {

        if(TestFileUtil.isInvalid(xmlResource, "graphml","xml")) {
            log.debug("Skipping invalid Graphml file: {}", xmlResource.getURI());
            return;
        }

        Path xmlFilePath = TestFileUtil.file(xmlResource).toPath();
        XmlTool.assertCanParseAsXml(xmlFilePath);

        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        System.out.println("== Input XML");
        XmlWriter xmlWriter_out = new DelegatingXmlWriter( //
                new Xml2AppendableWriter(System.out, //
                        AttributeOrderPerElement.AsWritten), //
                xmlWriter);

        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter_out);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);

        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2graphml);
        String xml_out = xmlWriter.resultString();

        // read into string;
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), StandardCharsets.UTF_8);

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in, null);
    }

}

