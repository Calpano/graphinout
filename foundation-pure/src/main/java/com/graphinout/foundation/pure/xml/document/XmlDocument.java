package com.graphinout.foundation.pure.xml.document;

import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.foundation.pure.xml.writer.XmlWriter;

import java.io.IOException;
import java.util.stream.Stream;

public class XmlDocument implements IXmlNode {

    private XmlElement root;

    @Override
    public Stream<IXmlNode> directChildren() {
        return Stream.of(root);
    }

    public void fire(XmlWriter writer) throws IOException {
        writer.documentStart();
        root.fire(writer);
        writer.documentEnd();
    }

    @Override
    public boolean hasEmptyContent(XML.XmlSpace xmlSpace) {
        return false;
    }

    /**
     * The whole document as XML text, including the XML declaration. Attributes are sorted lexicographically and text
     * is XML-encoded (see {@link Xml2StringWriter#Xml2StringWriter()}).
     * <p>
     * Unlike {@link #toXmlString()} (from {@link IXmlContent}), this always returns a String and writes a complete
     * document.
     */
    public String toXmlDocumentString() throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        fire(xmlWriter);
        return xmlWriter.resultString();
    }

    public XmlElement rootElement() {
        return root;
    }

    public void setRoot(XmlElement rootElement) {
        assert root == null;
        root = rootElement;
    }

}
