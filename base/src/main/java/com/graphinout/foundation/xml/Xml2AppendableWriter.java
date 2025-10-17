package com.graphinout.foundation.xml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry.comparingByKey;


/**
 * Simple XmlWriter implementation that writes to a StringWriter.
 * <p>
 * TODOuse XmlSpace to maybe normalize
 */
public class Xml2AppendableWriter extends XmlCharacter2AppendableWriter implements XmlWriter {

    private final XML.AttributeOrderPerElement attributeOrder;
    private @Nullable String openedTagName = null;

    /**
     * @param appendable               where to emit the XML to
     * @param attributeOrderPerElement use {@link XML.AttributeOrderPerElement#AsWritten} as a default
     * @param xmlEncodeOnWrite         default should be true; false helps debugging
     */
    public Xml2AppendableWriter(Appendable appendable, XML.AttributeOrderPerElement attributeOrderPerElement, boolean xmlEncodeOnWrite) {
        super(appendable, xmlEncodeOnWrite);
        this.attributeOrder = attributeOrderPerElement;
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
        }, XML.AttributeOrderPerElement.Lexicographic, true);
    }

    @Override
    public void characters(String characters, CharactersKind kind) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        super.characters(characters, kind);
    }

    @Override
    public void charactersEnd() {
        // no need write opening tag closing angle bracket
        super.charactersEnd();
    }

    @Override
    public void charactersStart() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        super.charactersStart();
    }

    @Override
    public void documentEnd() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        // Nothing special needed for string output
    }

    @Override
    public void documentStart() throws IOException {
        appendable.append(XML.XML_VERSION_1_0_ENCODING_UTF_8 + "\n");
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
        if (attributeOrder == XML.AttributeOrderPerElement.Lexicographic) {
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
        super.lineBreak();
    }

    @Override
    public void raw(String rawXml) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        super.raw(rawXml);
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
