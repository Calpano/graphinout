package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.xml.element.XmlDocument;

import java.io.IOException;

public class Xml2StringWriter extends Xml2AppendableWriter {


    public Xml2StringWriter() {
        super(new StringBuilder(), AttributeOrderPerElement.Lexicographic);
    }

    public Xml2StringWriter(AttributeOrderPerElement attributeOrderPerElement, boolean xmlEncodeOnWrite) {
        super(new StringBuilder(), AttributeOrderPerElement.Lexicographic, xmlEncodeOnWrite);
    }

    public static String toXmlString(XmlDocument xmlDoc) throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        if (xmlDoc != null) {
            xmlDoc.fire(xmlWriter);
        }
        return xmlWriter.resultString();
    }

    public void reset() {
        buffer().setLength(0);
    }

    public String resultString() {
        return buffer().toString();
    }

    public String toString() {
        return buffer().toString();
    }

    private StringBuilder buffer() {
        return (StringBuilder) appendable;
    }

}
