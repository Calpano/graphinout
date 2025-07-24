package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.Map;

public class XmlWriterSpy implements XmlWriter {

    private final StringBuilder outPut = new StringBuilder();

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        outPut.append("::characterData->").append(characterData);
    }

    @Override
    public void endDocument() throws IOException {
        outPut.append("::endDocument");
    }

    @Override
    public void endElement(String name) throws IOException {
        outPut.append("::endElement->").append(name);
    }

    public StringBuilder getOutPut() {
        return outPut;
    }

    @Override
    public void lineBreak() throws IOException {
        // ignored in tests
    }

    @Override
    public void startDocument() throws IOException {
        outPut.append("::startDocument->");
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        outPut.append("::startElement->").append(name).append("->").append(attributes);
    }

    @Override
    public void startCDATA() throws IOException {
        outPut.append("::startCDATA");
    }

    @Override
    public void endCDATA() throws IOException {
        outPut.append("::endCDATA");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        outPut.append("::raw->").append(rawXml);
    }

}
