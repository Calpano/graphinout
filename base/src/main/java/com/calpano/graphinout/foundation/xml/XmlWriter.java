package com.calpano.graphinout.foundation.xml;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * XML writer without namespace support, but we do allow colons in element names
 */
public interface XmlWriter {

    String CDATA_START = "<![CDATA[";
    String CDATA_END = "]]>";
    Logger _log = getLogger(XmlWriter.class);
    String XML_VERSION_1_0_ENCODING_UTF_8 = """
            <?xml version="1.0" encoding="UTF-8"?>""";

    /**
     * CDATA sections are signaled redundantly in two ways: 1) In {@link #cdataStart()} and <code>cdataEnd()</code>. 2)
     * in the flag in {@link #characterData(String, boolean)}.
     */
    void cdataEnd() throws IOException;

    /**
     * CDATA sections are signaled redundantly in two ways: 1) In <code>cdataStart()</code> and {@link #cdataEnd()}. 2)
     * in the flag in {@link #characterData(String, boolean)}.
     */
    void cdataStart() throws IOException;

    /**
     * CDATA sections are signaled redundantly in two ways: 1) In {@link #cdataStart()} and {@link #cdataEnd()}. 2) in
     * the flag in <code>characterData(String, boolean)</code>.
     *
     * @param characterData may contain line breaks.
     * @param isInCdata     is true iff a preceding {@link #cdataStart()} was not yet terminated by a matching
     *                      {@link #cdataEnd()}
     */
    void characterData(String characterData, boolean isInCdata) throws IOException;

    /**
     * Last call of {@link #characterData(String, boolean)}.
     *
     * @param isInCdata is true iff a preceding {@link #cdataStart()} was not yet terminated by a matching
     *                  {@link #cdataEnd()}
     */
    default void characterDataEnd(boolean isInCdata) throws IOException {}

    /**
     * Marks the beginning of character data. Followed by 1-n {@link #characterData(String, boolean)} calls, maybe
     * {@link #cdataStart()} and {@link #cdataEnd()} as well, and finally a {@link #characterDataEnd(boolean)}. The
     * <code>isInCdata</code> changes according to the CDATA sections.
     */
    default void characterDataStart(boolean isInCdata) throws IOException {}

    /**
     * Automatically split incoming string into non-CDATA and CDATA sections and call sub-methods.
     * {@link #characterData(String, boolean)}. {@link #cdataStart()}, {@link #cdataEnd()}
     *
     * @param s may contain CDATA sections, line breaks, processing instructions and any other syntax constructs.
     */
    default void characterDataWhichMayContainCdata(String s) throws IOException {
        // mini parser to split s at CDATA sections.
        int current = 0;
        //_log.info("parsing {}", s);
        int startCdata;
        while ((startCdata = s.indexOf(CDATA_START, current)) != -1) {
            if (startCdata > current) {
                String nonCdata = s.substring(current, startCdata);
                characterData(nonCdata, false);
            }
            cdataStart();
            int endCdata = s.indexOf(CDATA_END, startCdata + CDATA_START.length());
            if (endCdata == -1) {
                // Malformed CDATA, just write the rest as raw
                _log.warn("CDATA section starting at {} is not terminated.", startCdata);
                raw(s.substring(startCdata));
                return;
            }
            String cdata = s.substring(startCdata + CDATA_START.length(), endCdata);
            characterData(cdata, true);
            cdataEnd();
            current = endCdata + CDATA_END.length();
        }
        // Write any remaining non-CDATA content
        if (current < s.length()) {
            characterData(s.substring(current), false);
        }
    }

    default void characters(String completeCharacters, boolean isInCdata) throws IOException {
        characterDataStart(isInCdata);
        characterData(completeCharacters, isInCdata);
        characterDataEnd(isInCdata);
    }

    void documentEnd() throws IOException;

    void documentStart() throws IOException;

    /**
     * This allows creating malformed XML but makes understanding errors easier
     *
     * @param name redundantly, the ending elements name.
     */
    void elementEnd(String name) throws IOException;

    default void elementStart(String name) throws IOException {
        elementStart(name, Collections.emptyMap());
    }

    void elementStart(String name, Map<String, String> attributes) throws IOException;

    /**
     * Write a \n to the output. Line breaks can also occur within {@link #characterData(String, boolean)}.
     */
    void lineBreak() throws IOException;

    /**
     * Write a string that is excluded from XML encoding.
     *
     * @param rawXml may contain line breaks, processing instructions and any other syntax constructs.
     */
    void raw(String rawXml) throws IOException;

}
