package com.graphinout.reader.graphml;


import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.xml.Xml2StringWriter;
import com.graphinout.foundation.xml.XmlAssert;
import com.graphinout.foundation.xml.XmlTool;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;


@DisplayName("Graphml<->CJ")
public class Graphml2CjAndBackTest {

    static final String TEST_ID = "Gml2Cj2Gml";
    private static final Logger log = getLogger(Graphml2CjAndBackTest.class);

    /**
     * Verify the GraphMl files are valid XML
     */
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("GraphMl files parse as XML (Baseline 1)")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    void testAllXml(String displayPath, Resource xmlResource) throws Exception {
        // == OUT Pipeline
        Xml2StringWriter xmlWriter = new Xml2StringWriter();

        XmlTool.parseAndWriteXml(xmlResource, xmlWriter);
        String xml_in = xmlResource.getContentAsString();
        String xml_out = xmlWriter.resultString();

        XmlAssert.xAssertThatIsSameXml(xml_out, xml_in);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("Run XML->Graphml->Cj")
    @Disabled("Run manually to log intermediate CJ output")
    void testAllXml_Graphml_Cj(String displayPath, Resource xmlResource) throws Exception {
        if (TestFileUtil.isInvalid(xmlResource, "graphml", "xml")) {
            return;
        }
        // == OUT Pipeline
        Graphml2CjWriter graphml2cjWriter = new Graphml2CjWriter(new LoggingCjWriter(false));
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjWriter);

        // == IN
        try {
            XmlTool.parseAndWriteXml(xmlResource, xml2GraphmlWriter);
            if (TestFileUtil.isInvalid(xmlResource, "xml", "graphml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Throwable e) {
            //noinspection StatementWithEmptyBody
            if (TestFileUtil.isInvalid(xmlResource, "xml", "graphml")) {
                // perfect, we failed on an invalid file
            } else {
                throw e;
            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    @DisplayName("Test XML<->Graphml<->Cj (all Graphml)")
    void testAllXml_Graphml_Cj_Graphml_Xml(String displayPath, Resource xmlResource) throws Exception {
        log.info("TEST-" + TEST_ID + " on " + xmlResource.getURI());
        boolean isInvalidGraphml = TestFileUtil.isInvalid(xmlResource, "xml", "graphml");
//        if (isInvalidGraphml) {
//            log.info("Skipping invalid resource {}", xmlResource.getURI());
//            return;
//        }

        // == XML -> GraphML -> CJ doc
        // GraphML -> CJ
        Graphml2CjDocument graphml2cjStream = new Graphml2CjDocument();
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2cjStream);
        // == IN
        String xml_in = xmlResource.getContentAsString();
        try {
            XmlTool.parseAndWriteXml(xmlResource, xml2GraphmlWriter);
            if (isInvalidGraphml) {
                fail("Expected an exception on an invalid input "+xmlResource.getURI());
            }
        } catch (Throwable e) {
            if (isInvalidGraphml) {
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

        TestFileUtil.verifyOrRecord(xmlResource, TEST_ID, xml_out, xml_in, (actual, expected) -> {
            try {
                return GraphmlAssert.xAssertThatIsSameGraphml(actual, expected, () -> //
                        log.info("CJ.JSON:\n{}", cjDoc.toCjJsonString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, s -> s);
    }

}
