package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;

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
        XmlDocument xmlDoc = xml2doc.resultDoc();
        String xml_out = Xml2StringWriter.toXmlString(xmlDoc);

        TestFileUtil.verifyOrRecord(xmlFilePath, xml_out, xml_in, (actual,expected) -> {
            XmlAssert.xAssertThatIsSameXml(actual, expected);
            return true;
        });
    }

}
