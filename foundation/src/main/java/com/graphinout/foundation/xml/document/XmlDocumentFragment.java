package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.writer.Xml2AppendableWriter;
import com.graphinout.foundation.xml.XmlFragmentString;

import java.io.IOException;

/**
 * Represents the content (children) of an arbitrary {@link XmlElement}.
 *
 * @param xmlContent
 * @param xmlSpace
 */
public record XmlDocumentFragment(XmlContent xmlContent, XML.XmlSpace xmlSpace) {

    public static XmlDocumentFragment of(XmlContent xmlContent, XML.XmlSpace xmlSpace) {
        return new XmlDocumentFragment(xmlContent, xmlSpace);
    }

    public boolean isEmpty() {
        return xmlContent.hasEmptyContent(xmlSpace);
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

}
