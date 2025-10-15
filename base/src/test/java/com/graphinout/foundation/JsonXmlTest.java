package com.graphinout.foundation;

import com.graphinout.foundation.json.JSON;
import com.graphinout.foundation.json.value.IJsonXmlString;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;
import com.graphinout.foundation.xml.element.Xml2DocumentWriter;
import com.graphinout.foundation.xml.element.XmlDocumentFragment;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class JsonXmlTest {

    String xmlFragString = """
            <root xmlns="https://www.example.org/myNamespace">
            Hello <em>beautiful</em>
                   World! &amp;quot;
            </root>""";

    @Test
    void testXmlDocumentFragment2Json() throws Exception {
        XmlDocumentFragment xmlDocumentFragment = XmlDocumentFragment.of(Xml2DocumentWriter.parseToXmlContent(xmlFragString), XML.XmlSpace.preserve);
        String xml = xmlDocumentFragment.xmlContent().contentAsXml();
        assertThat(xml).isEqualTo(xmlFragString);

        IJsonXmlString jsonXml = JsonXml.toJsonXmlString(JavaJsonFactory.INSTANCE, xmlDocumentFragment);
        assertThat(jsonXml.rawXmlString()).isEqualTo(xmlFragString);
        assertThat(jsonXml.xmlSpace().jsonStringValue).isEqualTo(XML.XmlSpace.preserve.xmlAttValue);
    }

    @Test
    void testXmlFragmentString2Json() {
        XmlFragmentString xmlFragmentString = XmlFragmentString.of(xmlFragString, XML.XmlSpace.preserve);
        IJsonXmlString jsonXml = IJsonXmlString.of(JavaJsonFactory.INSTANCE, xmlFragmentString);
        assertThat(jsonXml.xmlSpace()).isEqualTo(JSON.XmlSpace.preserve);
        assertThat(jsonXml.rawXmlString()).isEqualTo(xmlFragString);
    }

}
