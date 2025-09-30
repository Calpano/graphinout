package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 */
public class Graphml2CjAndBackTest {

    private static final Logger log = getLogger(Graphml2CjAndBackTest.class);

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
    @DisplayName("XML->Graphml->Cj")
    void testAllXml_Graphml_Cj(String displayPath, Path xmlFilePath) throws Exception {
        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2CjWriter graphml2cjWriter = new Graphml2CjWriter(new LoggingCjWriter(false));
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjWriter);

        // == IN
        try {
            XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2GraphmlWriter);
            if (TestFileUtil.isInvalid(xmlFilePath, "xml", "graphml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFilePath, "xml", "graphml")) {
                // perfect, we failed on an invalid file
            } else {
                throw e;
            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    @DisplayName("XML->Graphml->Cj->Graphml->XML (Test all GraphMl files)")
    void testAllXml_Graphml_Cj_Graphml_Xml(String displayPath, Path xmlFilePath) throws Exception {
        // == XML -> GraphML -> CJ doc
        // GraphML -> CJ
        Graphml2CjDocument graphml2cjStream = new Graphml2CjDocument();
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjStream);
        // == IN
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        try {
            XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2GraphmlWriter);
            if (TestFileUtil.isInvalid(xmlFilePath, "xml", "graphml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFilePath, "xml", "graphml")) {
                // perfect, we failed on an invalid file
                return;
            } else {
                throw e;
            }
        }
        CjDocumentElement cjDoc = graphml2cjStream.resultDoc();


        // ==  CJ doc -> Graphml -> XML
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        // CJ -> GraphML
        CjDocument2Graphml cjDocument2Graphml = new CjDocument2Graphml(graphml2XmlWriter);
        cjDocument2Graphml.writeDocumentToGraphml(cjDoc);

        String xml_out = xmlWriter.string();

        TestFileUtil.verifyOrRecord(xmlFilePath, xml_out, xml_in, (actual, expected) -> //
                GraphmlAssert.xAssertThatIsSameGraphml(actual, expected, () -> //
                        log.info("CJ.JSON:\n{}", cjDoc.toCjJsonString())));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    @DisplayName("XML->Graphml->XML (Test all GraphMl files)")
    void testAllXml_Graphml_Xml(String displayPath, Path xmlFilePath) throws Exception {
        if (TestFileUtil.isInvalid(xmlFilePath, "xml", "graphml")) return;

        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2XmlWriter);

        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2GraphmlWriter);

        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        String xml_out = xmlWriter.string();

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in, null);
    }


}
