package com.graphinout.foundation.pure.xml.writer;

import com.graphinout.foundation.pure.xml.XML;

import com.graphinout.foundation.pure.xml.document.XmlDocument;
import org.jspecify.annotations.Nullable;
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

    /**
     * @deprecated use {@link XmlDocument#toXmlDocumentString()}
     */
    @Deprecated
    public static String toXmlString(XmlDocument xmlDoc) throws IOException {
        if (xmlDoc == null) {
            return "";
        }
        return xmlDoc.toXmlDocumentString();
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
