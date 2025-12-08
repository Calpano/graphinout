package com.graphinout.foundation.pure.xml;


import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonXmlString;

import java.util.Objects;

/**
 * Similar to a base.XmlDocumentFragment, but in plain string form.
 */
public final class XmlFragmentString {

    private final String rawXml;
    private final XML.XmlSpace xmlSpace;

    /**
     *
     */
    public XmlFragmentString(String rawXml, XML.XmlSpace xmlSpace) {
        this.rawXml = rawXml;
        this.xmlSpace = xmlSpace;
    }

    public static XmlFragmentString of(String rawXml, XML.XmlSpace xmlSpace) {
        return new XmlFragmentString(rawXml, xmlSpace);
    }

    public static XmlFragmentString ofPlainText(String plainTextValue) {
        return new XmlFragmentString(plainTextValue, XML.XmlSpace.default_);
    }

    public IJsonXmlString toJsonXmlString(IJsonFactory factory) {
        return IJsonXmlString.of(factory, rawXml(), xmlSpace().toJson_XmlSpace());
    }

    public String rawXml() {return rawXml;}

    public XML.XmlSpace xmlSpace() {return xmlSpace;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        XmlFragmentString that = (XmlFragmentString) obj;
        return Objects.equals(this.rawXml, that.rawXml) &&
                Objects.equals(this.xmlSpace, that.xmlSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawXml, xmlSpace);
    }

    @Override
    public String toString() {
        return "XmlFragmentString[" +
                "rawXml=" + rawXml + ", " +
                "xmlSpace=" + xmlSpace + ']';
    }


}
