package com.calpano.graphinout.foundation.xml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry.comparingByKey;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class Xml2AppendableWriter implements XmlWriter {

    public enum AttributeOrderPerElement {
        /** which is random, when coming from a SAX parser */
        AsWritten, Lexicographic
    }

    protected final Appendable appendable;
    private final boolean xmlEncodeOnWrite;
    private final AttributeOrderPerElement attributeOrder;
    private @Nullable String openedTagName = null;

    public Xml2AppendableWriter(Appendable appendable, AttributeOrderPerElement attributeOrderPerElement) {
        this(appendable, attributeOrderPerElement, true);
    }


    /**
     * @param xmlEncodeOnWrite default should be true; false helps debugging
     */
    public Xml2AppendableWriter(Appendable appendable, AttributeOrderPerElement attributeOrderPerElement, boolean xmlEncodeOnWrite) {
        this.appendable = appendable;
        this.attributeOrder = attributeOrderPerElement;
        this.xmlEncodeOnWrite = xmlEncodeOnWrite;
    }

    public static Xml2AppendableWriter createNoop() {
        return new Xml2AppendableWriter(new Appendable() {
            @Override
            public Appendable append(CharSequence csq) {
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) {
                return this;
            }

            @Override
            public Appendable append(char c) {
                return this;
            }
        }, AttributeOrderPerElement.Lexicographic);
    }

    public void characters(String characters, CharactersKind kind) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        if (kind == CharactersKind.CDATA) {
            // Don't encode CDATA content - write it raw
            raw(CDATA_START);
            appendable.append(characters);
            raw(CDATA_END);
        } else {
            // Only encode regular character data
            String toAppend = characters;
            if (xmlEncodeOnWrite) {
                toAppend = XmlTool.xmlEncode(characters);
            }
            appendable.append(toAppend);
        }
    }

    public void charactersEnd() {
    }

    public void charactersStart() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
    }

    @Override
    public void documentEnd() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        // Nothing special needed for string output
    }

    @Override
    public void documentStart() throws IOException {
        appendable.append(XML_VERSION_1_0_ENCODING_UTF_8 + "\n");
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        if (noContentWasWrittenAfterStartingLastElement()) {
            // No content was written, so self-close
            appendable.append("/>");
            resetSelfClosingElementMarker();
        } else {
            // proper XML closing element
            appendable.append("</");
            appendable.append(localName);
            appendable.append(">");
        }
    }

    /** auto-sort attributes on write */
    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        appendable.append("<");
        appendable.append(localName);
        List<Map.Entry<String, String>> attList = attributes.entrySet().stream().toList();
        if (attributeOrder == AttributeOrderPerElement.Lexicographic) {
            attList = new ArrayList<>(attList);
            attList.sort(comparingByKey());
        }
        for (Map.Entry<String, String> entry : attList) {
            appendable.append(" ");
            appendable.append(entry.getKey());
            appendable.append("=\"");
            // only XML encode the double quote character
            appendable.append(entry.getValue().replace("\"", "&quot;"));
            appendable.append("\"");
        }
        // note we didnt write the '>' or '/>' yet
        markStartedElementWithNoContentYetAs(localName);
    }

    @Override
    public void lineBreak() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        appendable.append("\n");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        appendable.append(rawXml);
    }

    private void markStartedElementWithNoContentYetAs(String name) {
        this.openedTagName = name;
    }

    private void maybeWriteOpeningTagClosingAngleBracket() throws IOException {
        if (noContentWasWrittenAfterStartingLastElement()) {
            appendable.append('>');
            resetSelfClosingElementMarker();
        }
    }

    private boolean noContentWasWrittenAfterStartingLastElement() {
        return openedTagName != null;
    }

    private void resetSelfClosingElementMarker() {
        this.openedTagName = null;
    }


}
