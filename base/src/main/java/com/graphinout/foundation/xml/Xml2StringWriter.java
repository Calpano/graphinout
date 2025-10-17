package com.graphinout.foundation.xml;

import com.graphinout.foundation.xml.element.XmlDocument;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public class Xml2StringWriter extends Xml2AppendableWriter {

    private final Consumer<String> onDone;

    public Xml2StringWriter() {
        this(XML.AttributeOrderPerElement.Lexicographic, true, null);
    }

    public Xml2StringWriter(XML.AttributeOrderPerElement attributeOrderPerElement, boolean xmlEncodeOnWrite, @Nullable Consumer<String> onDone) {
        super(new StringBuilder(), attributeOrderPerElement, xmlEncodeOnWrite);
        this.onDone = onDone;
    }

    // TODO move to XmlDocument
    public static String toXmlString(XmlDocument xmlDoc) throws IOException {
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        if (xmlDoc != null) {
            xmlDoc.fire(xmlWriter);
        }
        return xmlWriter.resultString();
    }

    @Override
    public void documentEnd() throws IOException {
        super.documentEnd();
        if (onDone != null) {
            onDone.accept(resultString());
        }
    }

    public void reset() {
        buffer().setLength(0);
    }

    public String resultString() {
        return buffer().toString();
    }

    public String toString() {
        return resultString();
    }

    private StringBuilder buffer() {
        return (StringBuilder) appendable;
    }

}
