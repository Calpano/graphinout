package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.Xml2StringWriter;
import com.graphinout.foundation.xml.testing.XmlAssert;
import com.graphinout.foundation.xml.util.XmlTool;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class XmlWriter2XmlDocumentTest {

    static final String TEST_ID = "Xml2Doc";

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#xmlResources")
    @DisplayName("XML->XmlDoc->Xml (All XML files)")
    void testAllXml_Xml_XmlDoc_Xml(String displayPath, Resource xmlResource) throws Exception {
        // == XML -> XmlDocument -> ZML
        Xml2DocumentWriter xml2doc = new Xml2DocumentWriter();
        // == IN
        String xml_in = xmlResource.getContentAsString();
        boolean invalidXml = TestFileUtil.isInvalid(xmlResource, "xml");
        try {
            XmlTool.parseAndWriteXml(xmlResource, xml2doc);
            if (invalidXml) {
                fail("Expected an exception on invalid file "+xmlResource.getURI());
            }
        } catch (Throwable e) {
            if (invalidXml) {
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

        TestFileUtil.verifyOrRecord(xmlResource, TEST_ID, xml_out, xml_in, (actual, expected) -> {
            XmlAssert.xAssertThatIsSameXml(actual, expected);
            return true;
        }, s -> s);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "äää", "a\nb", "&amp;", "&quot;", "&apos;", "&lt;", "&gt;"})
    void testEncodeDecode(String s) throws Exception {
        Xml2DocumentWriter xml2doc = new Xml2DocumentWriter();

        String xml = "<root>" + s + "</root>";
        XmlTool.parseAndWriteXml(xml, xml2doc);
        XmlDocument xmlDoc = xml2doc.resultDoc();
        // normal XML encoding happens here
        String xml_out = Xml2StringWriter.toXmlString(xmlDoc);
        assertThat(xml_out).isEqualTo(XML.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + xml);
    }


}
