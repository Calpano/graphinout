package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.foundation.xml.DelegatingXmlWriter;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import com.calpano.graphinout.foundation.xml.XmlWriter;

public class CharacterBuffer extends DelegatingXmlWriter implements XmlWriter {

    private final StringBuilder characterBuffer;

    public CharacterBuffer() {
        characterBuffer = new StringBuilder();
        Xml2AppendableWriter rawXmlWriter = new Xml2AppendableWriter(characterBuffer, Xml2AppendableWriter.AttributeOrderPerElement.Lexicographic);
        super.addWriter(rawXmlWriter);
    }

    /** Append non-XML content */
    public void append(String characterData) {
        characterBuffer.append(characterData);
    }

    public String getStringAndReset() {
        String s = characterBuffer.toString();
        characterBuffer.setLength(0);
        return s;
    }

    public boolean isEmpty() {
        return characterBuffer.isEmpty();
    }

}
