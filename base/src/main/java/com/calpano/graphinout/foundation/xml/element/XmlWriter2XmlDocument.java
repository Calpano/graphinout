package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.util.PowerStackOnClasses;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

public class XmlWriter2XmlDocument implements XmlWriter {

    private final StringBuilder buf = new StringBuilder();
    PowerStackOnClasses<XmlNode> stack = PowerStackOnClasses.create();
    @Nullable XmlDocument xmlDocument;

    @Override
    public void cdataEnd() throws IOException {
        buf.append(CDATA_END);
    }

    @Override
    public void cdataStart() throws IOException {
        buf.append(CDATA_START);
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        buf.append(characterData);
    }

    public void characterDataEnd(boolean isInCdata) throws IOException {
        String text = buf.toString();
        if (!text.isEmpty()) {
            stack.peek(XmlElement.class).addChild(new XmlText(text));
        }
    }

    public void characterDataStart(boolean isInCdata) throws IOException {
        buf.setLength(0);
    }

    @Override
    public void documentEnd() throws IOException {
        this.xmlDocument = stack.pop(XmlDocument.class);
    }

    @Override
    public void documentStart() throws IOException {
        stack.push(new XmlDocument());
    }

    @Override
    public void elementEnd(String name) throws IOException {
        // deprecated
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        XmlElement element = stack.pop(XmlElement.class);
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        XmlNode parent = stack.peek();
        XmlElement child = stack.push(new XmlElement(qName, attributes));
        if (parent instanceof XmlDocument doc) {
            doc.setRoot(child);
        } else if (parent instanceof XmlElement element) {
            element.addChild(child);
        } else {
            throw new IllegalStateException("Unexpected parent " + parent);
        }
    }

    @Override
    public void elementStart(String tagName, Map<String, String> attributes) throws IOException {
        // deprecated
    }

    @Override
    public void lineBreak() {
    }

    @Override
    public void raw(String rawXml) throws IOException {
        stack.peek(XmlElement.class).addChild(new XmlRaw(rawXml));
    }

    @Nullable
    public XmlDocument resultDoc() {
        return xmlDocument;
    }

}
