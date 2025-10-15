package com.graphinout.foundation.xml;

import java.io.IOException;

public class XmlCharacter2AppendableWriter implements XmlCharacterWriter {

    protected final Appendable appendable;
    private final boolean xmlEncodeOnWrite;

    public XmlCharacter2AppendableWriter(Appendable appendable, boolean xmlEncodeOnWrite) {
        this.appendable = appendable;
        this.xmlEncodeOnWrite = xmlEncodeOnWrite;
    }

    public void characters(String characters, CharactersKind kind) throws IOException {
        if (kind == CharactersKind.CDATA) {
            // Don't encode CDATA content - write it raw
            raw(XML.CDATA_START);
            raw(characters);
            raw(XML.CDATA_END);
        } else {
            // Do encode regular character data
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
    }

    @Override
    public void lineBreak() throws IOException {
        appendable.append("\n");
    }

    @Override
    public void raw(String rawXml) throws IOException {
        appendable.append(rawXml);
    }


}
