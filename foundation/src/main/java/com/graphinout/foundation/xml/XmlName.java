package com.graphinout.foundation.xml;

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
    public final boolean equals(Object o) {
        if (!(o instanceof XmlName xmlName)) return false;

        return uri.equals(xmlName.uri) && localName.equals(xmlName.localName) && qName.equals(xmlName.qName);
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + localName.hashCode();
        result = 31 * result + qName.hashCode();
        return result;
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
    public String toString() {
        return "XmlName{" + "local='" + localName + '\'' + ", uri='" + uri + '\'' + ", q='" + qName + '\'' + '}';
    }

    @Override
    public String uri() {
        return uri;
    }

}
