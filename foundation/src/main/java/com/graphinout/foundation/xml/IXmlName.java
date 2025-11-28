package com.graphinout.foundation.xml;

public interface IXmlName {

    static IXmlName of(String uri, String localName, String qName) {
        return new XmlName(uri, localName, qName);
    }

    static IXmlName of(String localName) {
        return new XmlName("", localName, localName);
    }

    /**
     * @param uri       '{@code http://example.org/schema/1.0}' -- the current namespace, often '' empty string.
     * @param localName 'myElement' - excludes all namespace prefixes
     * @param qName     'x:myElement' verbatim as written in XML
     * @return the localname, unless it is empty for some reason, then qName is used
     */
    static String tagName(String uri, String localName, String qName) {
        if (uri.isEmpty() || localName.isEmpty()) {
            return qName;
        } else {
            return localName;
        }
    }

    String localName();

    String qName();

    String uri();

}
