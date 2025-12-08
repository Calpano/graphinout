package com.graphinout.base.xml.util;

import com.graphinout.base.xml.XmlAssert;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import com.graphinout.base.xml.Xml2DocumentWriter;
import com.graphinout.foundation.pure.xml.XmlSerializer;
import com.graphinout.foundation.pure.xml.document.XmlDocument;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;

class XmlSerializerTest {

    /**
     * Verify the GraphMl files are valid XML
     */
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("GraphMl files parse as XML (Baseline 1)")
    @MethodSource("com.graphinout.testdata.TestFileProvider#xmlResources")
    void testAllXml(String displayPath, Resource xmlResource) throws Exception {
        // prep
        String xmlString = xmlResource.getContentAsString();
        Xml2DocumentWriter xml2doc = new Xml2DocumentWriter();
        XmlTool.parseAndWriteXml(xmlString, xml2doc);
        XmlDocument doc = xml2doc.resultDoc();
        assertThat(doc).isNotNull();

        // the actual test
        Object xml = XmlSerializer.toXmlString(doc, XML.XmlSpace.default_);
        assertThat(xml).isNotNull();

        String s = "";
        if (xml instanceof String xmlString1)
            s = xmlString1;
        else if (xml instanceof XmlFragmentString xmlFragmentString) {
            s = xmlFragmentString.rawXml();
        } else {
            Assertions.fail();
        }

        XmlAssert.xAssertThatIsSameXml(s, xmlString);
    }

}
