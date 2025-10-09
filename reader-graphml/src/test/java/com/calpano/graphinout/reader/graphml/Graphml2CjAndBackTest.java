package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

import static com.calpano.graphinout.foundation.TestFileUtil.file;
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
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    void testAllXml(String displayPath, Resource xmlResource) throws Exception {
        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        File xmlFile = file(xmlResource);
        Path xmlFilePath = xmlFile.toPath();

        XmlTool.parseAndWriteXml(xmlFile, xmlWriter);
        String xml_in = xmlResource.getContentAsString();
        String xml_out = xmlWriter.resultString();

        XmlAssert.xAssertThatIsSameXml(xml_out, xml_in);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("XML->Graphml->Cj")
    void testAllXml_Graphml_Cj(String displayPath, Resource xmlResource) throws Exception {
        if (TestFileUtil.isInvalid(xmlResource, "graphml", "xml")) {
            return;
        }
        File xmlFile = file(xmlResource);
        Path xmlFilePath = xmlFile.toPath();

        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2CjWriter graphml2cjWriter = new Graphml2CjWriter(new LoggingCjWriter(false));
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjWriter);

        // == IN
        try {
            XmlTool.parseAndWriteXml(xmlFile, xml2GraphmlWriter);
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
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("XML->Graphml->Cj->Graphml->XML (Test all GraphMl files)")
    void testAllXml_Graphml_Cj_Graphml_Xml(String displayPath, Resource xmlResource) throws Exception {
        // == XML -> GraphML -> CJ doc
        // GraphML -> CJ
        Graphml2CjDocument graphml2cjStream = new Graphml2CjDocument();
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjStream);
        // == IN
        String xml_in = xmlResource.getContentAsString();
        try {
            XmlTool.parseAndWriteXml(file(xmlResource), xml2GraphmlWriter);
            if (TestFileUtil.isInvalid(xmlResource, "xml", "graphml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlResource, "xml", "graphml")) {
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

        String xml_out = xmlWriter.resultString();

        TestFileUtil.verifyOrRecord(xmlResource, xml_out, xml_in, (actual, expected) ->
        {
            try {
                return GraphmlAssert.xAssertThatIsSameGraphml(actual, expected, () -> //
                        log.info("CJ.JSON:\n{}", cjDoc.toCjJsonString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, s -> s);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("XML->Graphml->XML (Test all GraphMl files)")
    void testAllXml_Graphml_Xml(String displayPath, Resource xmlResource) throws Exception {
        if (TestFileUtil.isInvalid(xmlResource, "xml", "graphml")) return;

        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2XmlWriter);

        XmlTool.parseAndWriteXml(file(xmlResource), xml2GraphmlWriter);

        String xml_in = xmlResource.getContentAsString();
        String xml_out = xmlWriter.resultString();

        GraphmlAssert.xAssertThatIsSameGraphml(xml_out, xml_in, null);
    }


}
