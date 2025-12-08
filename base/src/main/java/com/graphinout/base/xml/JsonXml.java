package com.graphinout.base.xml;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.foundation.pure.xml.document.XmlContent;
import com.graphinout.foundation.pure.xml.document.XmlDocumentFragment;
import com.graphinout.foundation.pure.xml.XmlFragmentString;

import java.io.IOException;

/**
 * Convert between XML and JSON ({@link IJsonXmlString})
 */
public class JsonXml {

    public static XmlDocumentFragment parseToXmlDocumentFragment(IJsonXmlString jsonXmlString) throws Exception {
        String xmlFragmentStringValue = jsonXmlString.rawXmlString();
        XmlContent xmlContent = Xml2DocumentWriter.parseToXmlContent(xmlFragmentStringValue);
        XmlDocumentFragment xmlDocFrag = XmlDocumentFragment.of(xmlContent, jsonXmlString.xmlSpace().toXml_XmlSpace());
        return xmlDocFrag;
    }

    /**
     * Translate an {@link XmlDocumentFragment} (a partial DOM) into an {@link IJsonXmlString} instance.
     *
     * @param xmlFrag input
     */
    public static IJsonXmlString toJsonXmlString(IJsonFactory factory, XmlDocumentFragment xmlFrag) {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        xmlFrag.xmlContent().directChildren().forEach(node -> {
            try {
                node.fire(xmlWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        String s = xmlWriter.resultString();
        return IJsonXmlString.of(factory, s, xmlFrag.xmlSpace().toJson_XmlSpace());
    }


    public static XmlFragmentString toXmlFragmentString(IJsonXmlString jsonXmlValue) {
        return XmlFragmentString.of(jsonXmlValue.rawXmlString(), jsonXmlValue.xmlSpace().toXml_XmlSpace());
    }

}
