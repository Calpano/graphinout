package com.graphinout.reader.ocif.document.types;

/**
 * Predefined MIME type constants used across examples and APIs. Use these instead of ad-hoc strings to avoid typos and
 * keep consistency.
 */
public enum MimeType {
    TEXT_PLAIN("text/plain"),
    IMAGE_PNG("image/png"),
    IMAGE_SVG_XML("image/svg+xml");

    private final String value;

    MimeType(String value) {
        this.value = value;
    }

    public OcifMimeType toOcif() {
        return OcifMimeType.of(value);
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Returns the string value of the MIME type, e.g. "text/plain".
     */
    public String value() {
        return value;
    }
}
