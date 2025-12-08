package com.graphinout.base.xml;

import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.document.XmlDocumentFragment;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class XmlDocFragTest {

    public static String xmlFragString = "<root xmlns=\"https://www.example.org/myNamespace\">\n" +
            "Hello <em>beautiful</em>\n" +
            "       World! &amp;quot;\n" +
            "</root>";

    @Test
    void testXmlDocumentFragment2Json() throws Exception {
        XmlDocumentFragment xmlDocumentFragment = XmlDocumentFragment.of(Xml2DocumentWriter.parseToXmlContent(xmlFragString), XML.XmlSpace.preserve);
        String xml = xmlDocumentFragment.xmlContent().contentAsXml();
        assertThat(xml).isEqualTo(xmlFragString);

        IJsonXmlString jsonXml = JsonXml.toJsonXmlString(JavaJsonFactory.INSTANCE, xmlDocumentFragment);
        assertThat(jsonXml.rawXmlString()).isEqualTo(xmlFragString);
        assertThat(jsonXml.xmlSpace().jsonStringValue).isEqualTo(XML.XmlSpace.preserve.xmlAttValue);
    }

}
