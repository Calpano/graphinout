package com.graphinout.foundation.xml;

import com.graphinout.foundation.text.StringFormatter;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Aggregates the multiple Java SAX API 'characters' calls into a single span of characters.
 * Also handles CDATA vs. normal, 'xml:space' = 'default' vs. 'preserve'.
 * Translates to {@link XmlCharacterWriter}.
 */
public class SaxCharBuffer {

    /** downstream */
    final XmlCharacterWriter xmlCharacterWriter;
    /** buffer content of the same CharactersKind */
    private final StringBuilder buffer = new StringBuilder();
    /** Are we already within a characters section? In which one? */
    private @Nullable CharactersKind currentKind;
    /** have we sent a start call yet for all sections? */
    private boolean sentStart;

    public SaxCharBuffer(XmlCharacterWriter xmlCharacterWriter) {
        this.xmlCharacterWriter = xmlCharacterWriter;
        clear();
    }

    /**
     * Append characters to running buffer
     *
     * @param characters may be the empty string
     */
    public void characters(String characters) throws IOException {
        buffer.append(characters);
    }

    /** All character sections are over */
    public void charactersEnd() throws IOException {
        boolean emittedSomething = maybeEmitCurrentSection();
        if (emittedSomething || this.sentStart) {
            xmlCharacterWriter.charactersEnd();
        }
        clear();
    }

    /** The current section is done. More sections, even of the same kind, can come */
    public void kindEnd() throws IOException {
        assert currentKind != null;

        // emit current section, although more sections of same kind may come
        // otherwise we would forget the kind we had
        maybeEmitCurrentSection();

        // if we had been in CDATA we are no longer
        currentKind = null;
    }

    public void kindStart(CharactersKind kind) throws IOException {
        maybeEmitCurrentSection();
        currentKind = kind;
    }

    private void clear() {
        sentStart = false;
        currentKind = CharactersKind.Default;
        buffer.setLength(0);
    }

    /**
     * If we have a non-empty buffer, emit it.
     * @return if anything was emitted
     */
    private boolean maybeEmitCurrentSection() throws IOException {
        // we can completely skip empty sections
        if (buffer.isEmpty()) return false;

        if (currentKind == null) {
            // if nobody told us we are parsing CDATA, we must be default
            currentKind = CharactersKind.Default;
        }

        if (!sentStart) {
            // we never sent start
            xmlCharacterWriter.charactersStart();
            sentStart = true;
        }
        // we need early line break normalization across platforms (CR LF->CR), even in CDATA
        String buffered = buffer.toString();
        buffered = StringFormatter.normalizeLineBreaks(buffered);
        xmlCharacterWriter.characters(buffered, currentKind);

        buffer.setLength(0);
        currentKind = null;
        return true;
    }

}
