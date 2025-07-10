package com.calpano.graphinout.foundation.xml;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * XML writer without namespace support, but we do allow colons in element names
 */
public interface XmlWriter {


    void characterData(String characterData) throws IOException;

    void endDocument() throws IOException;

    /**
     * This allows to create malformed XML, but makes understanding errors easier
     *
     * @param name
     */
    void endElement(String name) throws IOException;

    /**
     * Write a \n to the output
     */
    void lineBreak() throws IOException;

    void startDocument() throws IOException;

    default void startElement(String name) throws IOException {
        startElement(name, Collections.emptyMap());
    }

    void startElement(String name, Map<String, String> attributes) throws IOException;


}
