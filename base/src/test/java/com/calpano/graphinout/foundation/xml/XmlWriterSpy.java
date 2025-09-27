package com.calpano.graphinout.foundation.xml;

import java.util.Map;
import java.util.TreeMap;

public class XmlWriterSpy implements XmlWriter {

    private final StringBuilder out = new StringBuilder();

    @Override
    public void cdataEnd() {
        out.append("</CDATA>");
    }

    @Override
    public void cdataStart() {
        out.append("<CDATA>");
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) {
        out.append("<chars inCdata=").append(isInCdata).append(">").append(characterData).append("</chars>");
    }

    @Override
    public void documentEnd() {
        out.append("</DOCUMENT>");
    }

    @Override
    public void documentStart() {
        out.append("<DOCUMENT>");
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) {
        out.append("</element ").append(localName).append(">");
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) {
        out.append("<element ").append(localName).append(">");
        out.append("<atts ").append(new TreeMap<>(attributes)).append(">");
    }

    public StringBuilder getOut() {
        return out;
    }

    @Override
    public void lineBreak() {
        out.append("<NEWLINE />");
    }

    @Override
    public void raw(String rawXml) {
        out.append("<raw>").append(rawXml).append("</raw>");
    }

}
