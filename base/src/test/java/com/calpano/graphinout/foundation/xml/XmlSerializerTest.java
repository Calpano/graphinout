package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.xml.element.Xml2DocumentWriter;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
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
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlResources")
    void testAllXml(String displayPath, Resource xmlResource) throws Exception {
        // prep
        String xmlString = xmlResource.getContentAsString();
        Xml2DocumentWriter xml2doc = new Xml2DocumentWriter();
        XmlTool.parseAndWriteXml(xmlString, xml2doc);
        XmlDocument doc = xml2doc.resultDoc();
        assertThat(doc).isNotNull();

        // the actual test

        // FIXME
        Xml2StringWriter w = new Xml2StringWriter();
        doc.fire(w);
        String xml2 = w.resultString();

        Object xml1 = XmlSerializer.toXmlString(doc, XML.XmlSpace.default_);
        assertThat(xml1).isNotNull();

        String s = "";
        if (xml1 instanceof String xmlString1)
            s = xmlString1;
        else if (xml1 instanceof XmlFragmentString xmlFragmentString) {
            s = xmlFragmentString.rawXml();
        } else {
            Assertions.fail();
        }

        XmlAssert.xAssertThatIsSameXml(s, xmlString);
    }

}
