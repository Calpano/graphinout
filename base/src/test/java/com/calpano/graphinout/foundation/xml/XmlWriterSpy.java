package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class XmlWriterSpy implements XmlWriter {

    private final StringBuilder out = new StringBuilder();

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        out.append("<chars inCdata=").append(isInCdata).append(">").append(characterData).append("</chars>");
    }

    @Override
    public void cdataEnd() throws IOException {
        out.append("</CDATA>");
    }

    @Override
    public void documentEnd() throws IOException {
        out.append("</DOCUMENT>");
    }

    @Override
    public void elementEnd(String name) throws IOException {
        out.append("</element ").append(name).append(">");
    }

    public StringBuilder getOut() {
        return out;
    }

    @Override
    public void lineBreak() throws IOException {
        out.append("<NEWLINE />");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        out.append("<raw>").append(rawXml).append("</raw>");
    }

    @Override
    public void cdataStart() throws IOException {
        out.append("<CDATA>");
    }

    @Override
    public void documentStart() throws IOException {
        out.append("<DOCUMENT>");
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        out.append("<element ").append(name).append(">");
        out.append("<atts ").append(new TreeMap<>(attributes)).append(">");
    }

}
