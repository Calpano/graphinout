package com.graphinout.base.xml;

import com.graphinout.foundation.json.value.IJsonXmlString;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.document.XmlDocumentFragment;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.JsonXmlTest.xmlFragString;

public class XmlDocFragTest {

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
