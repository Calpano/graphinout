package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.util.PowerStackOnClasses;
import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * If we XML encode on writing, we must XML decode on reading.
 */
public class XmlWriter2XmlDocument implements XmlWriter {

    /** accumulate CDATA and normal sections */
    private final StringBuilder buf = new StringBuilder();
    PowerStackOnClasses<XmlNode> stack = PowerStackOnClasses.create();
    @Nullable XmlDocument xmlDocument;

    public void characters(String characters, CharactersKind kind) {
        if (characters.isEmpty()) return;

        String s = characters;
        switch (kind) {
            case Default, PreserveWhitespace -> {
                s = XmlTool.xmlDecode(s);
            }
            case IgnorableWhitespace -> {
                assert s.trim().isEmpty();
            }
            case CDATA -> {
                // dont touch it
            }
        }

        stack.peek(XmlText.class).addSection(s, kind);
    }

    public void charactersEnd() {
        stack.pop(XmlText.class);
    }

    public void charactersStart() {
        XmlElement parent = stack.peek(XmlElement.class);
        XmlText text = stack.push(new XmlText());
        parent.addChild(text);
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
