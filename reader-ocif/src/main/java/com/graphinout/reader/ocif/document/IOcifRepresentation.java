package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.impl.OcifRepresentation;

/**
 * OCIF Resource Representation.
 * <p>
 * Spec excerpts (schema.json $defs.representation): Either content or location MUST be present. If a data: TYPE_URI is used
 * in {@code location}, the content and MIME-type properties are implicitly defined already.
 */
public interface IOcifRepresentation {

    static IOcifRepresentation ofContent(String content, String mimeType) {
        return new OcifRepresentation(content, null, mimeType);
    }

    static IOcifRepresentation ofLocation(String location, String mimeType) {
        return new OcifRepresentation(null, location, mimeType);
    }

    /** content (string): Inline content (e.g., base64 data or text). */
    String content();

    /**
     * location (string): Storage location (relative/absolute TYPE_URI). If a data: TYPE_URI is used, content/mimeType are
     * implicit.
     */
    String location();

    /** mimeType (string): IANA MIME Type of the content. */
    String mimeType();

}
