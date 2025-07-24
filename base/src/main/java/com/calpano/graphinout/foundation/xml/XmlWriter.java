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

    /**
     * TODO what about CDATA sections inside?
     *
     * @param characterData may contain line breaks.
     * @param isInCdata is true iff a preceding {@link #startCDATA()} was not yet terminated by a matching {@link #endCDATA()}
     */
    void characterData(String characterData, boolean isInCdata) throws IOException;

    /**
     * Automatically split incoming string into non-CDATA and CDATA sections and call sub-methods.
     * {@link #characterData(String, boolean)}. {@link #startCDATA()}, {@link #endCDATA()}
     *
     * @param s may contain CDATA sections, line breaks, processing instructions and any other syntax constructs.
     */
    default void characterDataWhichMayContainCdata(String s) throws IOException {
        // mini parser to split s at CDATA sections.
        int current = 0;
        _log.info("parsing "+s);
        int startCdata;
        while ((startCdata = s.indexOf(CDATA_START, current)) != -1) {
            if (startCdata > current) {
                String nonCdata = s.substring(current, startCdata);
                characterData(nonCdata, false);
            }
            startCDATA();
            int endCdata = s.indexOf(CDATA_END, startCdata + CDATA_START.length());
            if (endCdata == -1) {
                // Malformed CDATA, just write the rest as raw
                _log.warn("CDATA section starting at {} is not terminated.", startCdata);
                raw(s.substring(startCdata));
                return;
            }
            String cdata = s.substring(startCdata + CDATA_START.length(), endCdata);
            characterData(cdata, true);
            endCDATA();
            current = endCdata + CDATA_END.length();
        }
        // Write any remaining non-CDATA content
        if (current < s.length()) {
            characterData(s.substring(current), false);
        }
    }

    void endCDATA() throws IOException;

    void endDocument() throws IOException;

    /**
     * This allows to create malformed XML, but makes understanding errors easier
     *
     * @param name redundantly, the ending elements name.
     */
    void endElement(String name) throws IOException;

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

    void startCDATA() throws IOException;

    void startDocument() throws IOException;

    default void startElement(String name) throws IOException {
        startElement(name, Collections.emptyMap());
    }

    void startElement(String name, Map<String, String> attributes) throws IOException;

}
