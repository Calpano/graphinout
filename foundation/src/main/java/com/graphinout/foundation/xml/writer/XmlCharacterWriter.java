package com.graphinout.foundation.xml.writer;

import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.XML;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * For organising the code all char stuff is here.
 * <p>
 * Character data is modelled as a non-nestable sequence of different kinds of sections. Each section has a
 * {@link CharactersKind}. Due to the streaming nature of XML, several sections might be of the same kind. The overall
 * protocol is, given the following data:
 * <p>
 * {@code <someTag>AAAAA BBBBB</someTag>} where AAAA and BBBB denote different {@link CharactersKind} of characters,
 * e.g. AAAA is normal and BBBB is CDATA.
 *
 * <li>start characters: {@link #charactersStart()}</li>
 * <li>actual characters of {@link CharactersKind} A: {@link #characters(String, CharactersKind)}</li>
 * <li>actual characters of {@link CharactersKind} B: {@link #characters(String, CharactersKind)}</li>
 * <li>end characters {@link #charactersEnd()}</li>
 */
public interface XmlCharacterWriter {

    Logger _log = getLogger(XmlCharacterWriter.class);

    /**
     * Automatically split incoming string into non-CDATA and CDATA sections and call sub-methods.
     *
     * @param s may contain CDATA sections, line breaks, processing instructions and any other syntax constructs.
     */
    default void characterDataWhichMayContainCdata(String s) throws IOException {
        // mini parser to split s at CDATA sections.
        int current = 0;
        //_log.info("parsing {}", s);
        int startCdata;
        while ((startCdata = s.indexOf(XML.CDATA_START, current)) != -1) {
            if (startCdata > current) {
                String nonCdata = s.substring(current, startCdata);
                characters(nonCdata, CharactersKind.Default);
            }
            int endCdata = s.indexOf(XML.CDATA_END, startCdata + XML.CDATA_START.length());
            if (endCdata == -1) {
                // Malformed CDATA, just write the rest as raw
                _log.warn("CDATA section starting at {} is not terminated.", startCdata);
                raw(s.substring(startCdata));
                return;
            }
            String cdata = s.substring(startCdata + XML.CDATA_START.length(), endCdata);
            characters(cdata, CharactersKind.CDATA);
            current = endCdata + XML.CDATA_END.length();
        }
        // Write any remaining non-CDATA content
        if (current < s.length()) {
            characters(s.substring(current), CharactersKind.Default);
        }
    }

    /**
     * NOTE: Unlike the SAX-Java API, this method always delivers complete character chunks. This puts some burden on
     * the memory, but makes the API much simpler to use.
     */
    void characters(String characters, CharactersKind kind) throws IOException;

    /** new API */
    void charactersEnd() throws IOException;

    /**
     * Marks the beginning of character data. Followed by 1-n {@link #characters(String, CharactersKind)} calls, and
     * finally a {@link #charactersEnd()}.
     */
    void charactersStart() throws IOException;

    /**
     * Write a \n to the output. Line breaks can also occur within {@link #characters(String, CharactersKind)}, but
     * those are usually not signalled via this method. These line breaks are used fore pretty-printing and can
     * selectively be ignored by some implementations.
     */
    void lineBreak() throws IOException;

    /**
     * Write a string that is excluded from XML encoding. This method is never called when parsing XML from text. It is
     * intended to be called from code which emits XML. The rawXml is written character for character as given here. No
     * escaping/encoding of any kind is applied (except the UTF-8 character-to-byte encoding).
     *
     * @param rawXml may contain line breaks, processing instructions and any other syntax constructs. MUST be a valid
     *               XML fragment to get valid XML.
     */
    void raw(String rawXml) throws IOException;

}
