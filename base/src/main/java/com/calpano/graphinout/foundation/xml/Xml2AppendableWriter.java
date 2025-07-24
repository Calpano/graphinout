package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.Map;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class Xml2AppendableWriter implements XmlWriter {

    protected final Appendable appendable;
    private boolean insideCDATA = false;

    public Xml2AppendableWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        if (insideCDATA) {
            raw(characterData);
        } else {
            appendable.append(XmlTool.xmlEncode(characterData));
        }
    }

    @Override
    public void endCDATA() throws IOException {
        raw("]]>");
        insideCDATA = false;  // Track CDATA state
    }

    @Override
    public void endDocument() throws IOException {
        // Nothing special needed for string output
    }

    @Override
    public void endElement(String name) throws IOException {
        appendable.append("</");
        appendable.append(name);
        appendable.append(">");
    }

    @Override
    public void lineBreak() throws IOException {
        appendable.append("\n");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        appendable.append(rawXml);
    }

    @Override
    public void startCDATA() throws IOException {
        System.out.println("CDATA in Xml2AppendableWriter");
        raw("<![CDATA[");
        insideCDATA = true;  // Track CDATA state
    }

    @Override
    public void startDocument() throws IOException {
        // appendable.append("<?xml version='1.0' encoding='utf-8'?>");
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        appendable.append("<");
        appendable.append(name);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            appendable.append(" ");
            appendable.append(entry.getKey());
            appendable.append("=\"");
            appendable.append(entry.getValue());
            appendable.append("\"");
        }
        appendable.append(">");
    }

}
