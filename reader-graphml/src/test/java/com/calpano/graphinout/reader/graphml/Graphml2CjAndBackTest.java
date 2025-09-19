package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.reader.graphml.cj.Cj2GraphmlWriter;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;


/**
 *
 */
public class Graphml2CjAndBackTest {


    /**
     * Verify the GraphMl files are valid XML
     */
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("GraphMl files parse as XML (Baseline 1)")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    void testAllXml(String displayPath, Path xmlFilePath) throws Exception {
        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();

        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xmlWriter);
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        String xml_out = xmlWriter.string();

        XmlAssert.xAssertThatIsSameXml(xml_out, xml_in);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    @DisplayName("Test all GraphMl files as: XML->Graphml->Cj->Graphml->CJ")
    void testAllXml_Graphml_Cj_Graphml_Xml(String displayPath, Path xmlFilePath) throws Exception {
        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        /* receive CJ events -> send Graphml events  */
        Cj2GraphmlWriter cj2GraphmlWriter = new Cj2GraphmlWriter(graphml2XmlWriter);
        Graphml2CjWriter graphml2cjWriter = new Graphml2CjWriter(cj2GraphmlWriter);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjWriter);

        // == IN
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        try {
            XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2GraphmlWriter);
            if (TestFileUtil.isInvalid(xmlFilePath)) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFilePath)) {
                // perfect, we failed on an invalid file
                return;
            } else {
                throw e;
            }
        }

        String xml_out = xmlWriter.string();
        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    @DisplayName("Test all GraphMl files as: XML->Graphml->XML (Baseline 2)")
    void testAllXml_Graphml_Xml(String displayPath, Path xmlFilePath) throws Exception {
        if (TestFileUtil.isInvalid(xmlFilePath)) return;

        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2XmlWriter);

        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2GraphmlWriter);

        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        String xml_out = xmlWriter.string();

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in);
    }


}
