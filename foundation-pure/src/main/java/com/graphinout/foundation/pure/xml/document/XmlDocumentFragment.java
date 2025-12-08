package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import com.graphinout.foundation.pure.xml.writer.Xml2AppendableWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents the content (children) of an arbitrary {@link XmlElement}.
 *
 */
public final class XmlDocumentFragment {

    private final XmlContent xmlContent;
    private final XML.XmlSpace xmlSpace;

    /**
     * @param xmlContent
     * @param xmlSpace
     */
    public XmlDocumentFragment(XmlContent xmlContent, XML.XmlSpace xmlSpace) {
        this.xmlContent = xmlContent;
        this.xmlSpace = xmlSpace;
    }

    public static XmlDocumentFragment of(XmlContent xmlContent, XML.XmlSpace xmlSpace) {
        return new XmlDocumentFragment(xmlContent, xmlSpace);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        XmlDocumentFragment that = (XmlDocumentFragment) obj;
        return Objects.equals(this.xmlContent, that.xmlContent) && Objects.equals(this.xmlSpace, that.xmlSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xmlContent, xmlSpace);
    }

    public boolean isEmpty() {
        return xmlContent.hasEmptyContent(xmlSpace);
    }

    @Override
    public String toString() {
        return "XmlDocumentFragment[" + "xmlContent=" + xmlContent + ", " + "xmlSpace=" + xmlSpace + ']';
    }

    public XmlFragmentString toXmlFragmentString() {
        StringBuilder b = new StringBuilder();
        Xml2AppendableWriter xml2AppendableWriter = new Xml2AppendableWriter(b, XML.AttributeOrderPerElement.AsWritten, true);
        xmlContent.directChildren().forEach(node -> {
            try {
                node.fire(xml2AppendableWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return XmlFragmentString.of(b.toString(), xmlSpace);
    }

    public XmlContent xmlContent() {return xmlContent;}

    public XML.XmlSpace xmlSpace() {return xmlSpace;}


}
