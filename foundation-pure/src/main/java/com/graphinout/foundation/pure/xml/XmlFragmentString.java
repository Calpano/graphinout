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

    /**
     * @param plainTextValue plain text, as an application sees it; never already encoded. It is XML-encoded here, so
     *                       that {@link #rawXml()} is well-formed. {@link #toPlainText()} gives the text back.
     */
    public static XmlFragmentString ofPlainText(String plainTextValue) {
        return new XmlFragmentString(XML.xmlEncode(plainTextValue), XML.XmlSpace.default_);
    }

    public IJsonXmlString toJsonXmlString(IJsonFactory factory) {
        return IJsonXmlString.of(factory, rawXml(), xmlSpace().toJson_XmlSpace());
    }

    /** the XML source of this fragment: text in it is encoded. For the text itself, see {@link #toPlainText()}. */
    public String rawXml() {return rawXml;}

    /**
     * The text of this fragment, decoded as an XML parser would: predefined entities, numeric character references and
     * CDATA sections become the characters they stand for. Unknown or malformed references are kept as they are.
     * <p>
     * A fragment with other markup (elements, comments, processing instructions) has no plain text form; for these,
     * {@link #rawXml()} is returned unchanged.
     */
    public String toPlainText() {
        if (rawXml == null) return null;
        String text = XML.decodeText(rawXml);
        return text == null ? rawXml : text;
    }

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
