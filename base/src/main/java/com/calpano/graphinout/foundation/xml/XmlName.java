package com.calpano.graphinout.foundation.xml;

public class XmlName implements IXmlName {

    private final String uri;
    private final String localName;
    private final String qName;

    public XmlName(String uri, String localName, String qName) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
    }

    @Override
    public String localName() {
        return localName;
    }

    @Override
    public String qName() {
        return qName;
    }

    @Override
    public String uri() {
        return uri;
    }

}
