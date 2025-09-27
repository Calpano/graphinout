package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.util.PowerStackOnClasses;
import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.util.Map;

public class XmlWriter2XmlDocument implements XmlWriter {

    private final StringBuilder buf = new StringBuilder();
    PowerStackOnClasses<XmlNode> stack = PowerStackOnClasses.create();
    @Nullable XmlDocument xmlDocument;

    @Override
    public void cdataEnd() {
        buf.append(CDATA_END);
    }

    @Override
    public void cdataStart() {
        buf.append(CDATA_START);
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) {
        buf.append(characterData);
    }

    public void characterDataEnd(boolean isInCdata) {
        String text = buf.toString();
        if (!text.isEmpty()) {
            stack.peek(XmlElement.class).addChild(new XmlText(text));
        }
    }

    public void characterDataStart(boolean isInCdata) {
        buf.setLength(0);
    }

    @Override
    public void documentEnd() {
        this.xmlDocument = stack.pop(XmlDocument.class);
    }

    @Override
    public void documentStart() {
        stack.push(new XmlDocument());
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) {
        stack.pop(XmlElement.class);
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) {
        XmlNode parent = stack.peek();
        IXmlName xmlName = IXmlName.of(uri, localName, qName);
        XmlElement child = stack.push(new XmlElement(xmlName, attributes));
        if (parent instanceof XmlDocument doc) {
            doc.setRoot(child);
        } else if (parent instanceof XmlElement element) {
            element.addChild(child);
        } else {
            throw new IllegalStateException("Unexpected parent " + parent);
        }
    }

    @Override
    public void lineBreak() {
    }

    @Override
    public void raw(String rawXml) {
        stack.peek(XmlElement.class).addChild(new XmlRaw(rawXml));
    }

    @Nullable
    public XmlDocument resultDoc() {
        return xmlDocument;
    }

}
