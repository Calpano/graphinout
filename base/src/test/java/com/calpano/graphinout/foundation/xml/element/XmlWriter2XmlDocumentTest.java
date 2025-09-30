package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;

class XmlWriter2XmlDocumentTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlFiles")
    @DisplayName("XML->XmlDoc->Xml (All XML files)")
    void testAllXml_Xml_XmlDoc_Xml(String displayPath, Path xmlFilePath) throws Exception {
        // == XML -> XmlDocument -> ZML
        XmlWriter2XmlDocument xml2doc = new XmlWriter2XmlDocument();
        // == IN
        String xml_in = FileUtils.readFileToString(xmlFilePath.toFile(), UTF_8);
        try {
            XmlTool.parseAndWriteXml(xmlFilePath.toFile(), xml2doc);
            if (TestFileUtil.isInvalid(xmlFilePath, "xml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFilePath, "xml")) {
                // perfect, we failed on an invalid file
                return;
            } else {
                throw e;
            }
        }
        XmlDocument xmlDoc = xml2doc.resultDoc();
        String xml_out = Xml2StringWriter.toXmlString(xmlDoc);
        // XML parsing enforces HTML entity normalisation, otherwise the SAX parser dies
        // so we must adapt out expectations, too
        //xml_in = XmlTool.htmlEntitiesToDecimalEntities(xml_in);

        TestFileUtil.verifyOrRecord(xmlFilePath, xml_out, xml_in, (actual, expected) -> {
            XmlAssert.xAssertThatIsSameXml(actual, expected);
            return true;
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;"})
    void testEncodeDecode(String s) throws Exception {
        XmlWriter2XmlDocument xml2doc = new XmlWriter2XmlDocument();

        String xml = "<root>" + s + "</root>";
        XmlTool.parseAndWriteXml(xml, xml2doc);
        XmlDocument xmlDoc = xml2doc.resultDoc();
        // normal XML encoding happens here
        String xml_out = Xml2StringWriter.toXmlString(xmlDoc);
        assertThat(xml_out).isEqualTo(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + xml);
    }


}
