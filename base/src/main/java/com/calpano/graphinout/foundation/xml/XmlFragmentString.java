package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonXmlString;

/**
 * Similar to an {@link com.calpano.graphinout.foundation.xml.element.XmlDocumentFragment}, but in plain string form.
 *
 */
public record XmlFragmentString(String rawXml, XML.XmlSpace xmlSpace) {

    public static XmlFragmentString of(String rawXml, XML.XmlSpace xmlSpace) {
        return new XmlFragmentString(rawXml, xmlSpace);
    }

    public static XmlFragmentString ofPlainText(String plainTextValue) {
        return new XmlFragmentString(plainTextValue, XML.XmlSpace.default_);
    }

    public IJsonXmlString toJsonXmlString(IJsonFactory factory) {
        return IJsonXmlString.of(factory, rawXml(), xmlSpace().toJson_XmlSpace());
    }

}
