package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import io.github.classgraph.Resource;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static com.calpano.graphinout.foundation.TestFileUtil.file;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;

class XmlWriter2XmlDocumentTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlResources")
    @DisplayName("XML->XmlDoc->Xml (All XML files)")
    void testAllXml_Xml_XmlDoc_Xml(String displayPath, Resource xmlResource) throws Exception {
        // == XML -> XmlDocument -> ZML
        Xml2DocumentWriter xml2doc = new Xml2DocumentWriter();
        // == IN
        File xmlFile = file(xmlResource);
        String xml_in = FileUtils.readFileToString(xmlFile, UTF_8);
        try {
            XmlTool.parseAndWriteXml(xmlFile, xml2doc);
            if (TestFileUtil.isInvalid(xmlFile.toPath(), "xml")) {
                fail("Expected an exception on an invalid file");
            }
        } catch (Exception e) {
            if (TestFileUtil.isInvalid(xmlFile.toPath(), "xml")) {
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

        TestFileUtil.verifyOrRecord(xmlResource, xml_out, xml_in, (actual, expected) -> {
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
        assertThat(xml_out).isEqualTo(XmlWriter.XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + xml);
    }


}
