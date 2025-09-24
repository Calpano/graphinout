package com.calpano.graphinout.foundation.xml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

import static java.util.Map.Entry.comparingByKey;

/**
 * Simple XmlWriter implementation that writes to a StringWriter
 */
public class Xml2AppendableWriter implements XmlWriter {

    protected final Appendable appendable;
    private @Nullable String openedTagName = null;

    public Xml2AppendableWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    public static Xml2AppendableWriter createNoop() {
        return new Xml2AppendableWriter(new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                return this;
            }

            @Override
            public Appendable append(char c) throws IOException {
                return this;
            }
        });
    }

    @Override
    public void cdataEnd() throws IOException {
        raw(CDATA_END);
    }

    @Override
    public void cdataStart() throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        raw(CDATA_START);
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        if (isInCdata) {
            // Don't encode CDATA content - write it raw
            appendable.append(characterData);
        } else {
            // Only encode regular character data
            appendable.append(XmlTool.xmlEncode(characterData));
        }
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
    public void elementEnd(String name) throws IOException {
        if (this.openedTagName != null) {
            // No content was written, so self-close
            appendable.append("/>");
            this.openedTagName = null;
        } else {
            // proper XML closing element
            appendable.append("</");
            appendable.append(name);
            appendable.append(">");
        }
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        maybeWriteOpeningTagClosingAngleBracket();
        appendable.append("<");
        appendable.append(name);
        for (Map.Entry<String, String> entry : attributes.entrySet().stream().sorted(comparingByKey()).toList()) {
            appendable.append(" ");
            appendable.append(entry.getKey());
            appendable.append("=\"");
            // only XML encode the double quote character
            appendable.append(entry.getValue().replace("\"", "&quot;"));
            appendable.append("\"");
        }
        // note we didnt write the '>' or '/>' yet
        this.openedTagName = name;
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

    private void maybeWriteOpeningTagClosingAngleBracket() throws IOException {
        if (this.openedTagName != null) {
            appendable.append('>');
            this.openedTagName = null;
        }
    }

}
