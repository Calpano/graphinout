package com.graphinout.reader.ocif.document;

/**
 * OCIF Resource Representation.
 * <p>
 * Spec excerpts (schema.json $defs.representation): Either content or location MUST be present. If a data: URI is used
 * in {@code location}, the content and MIME-type properties are implicitly defined already.
 * <ul>
 *   <li>location (string): Storage location (relative/absolute URI). If a data: URI is used, content/mimeType are implicit.</li>
 *   <li>mimeType (string): IANA MIME Type of the content.</li>
 *   <li>content (string): Inline content (e.g., base64 data or text).</li>
 * </ul>
 */
public class OcifRepresentation {

    private String location;
    private String mimeType;
    private String content;

    public String getLocation() { return location; }
    public OcifRepresentation setLocation(String location) { this.location = location; return this; }

    public String getMimeType() { return mimeType; }
    public OcifRepresentation setMimeType(String mimeType) { this.mimeType = mimeType; return this; }

    public String getContent() { return content; }
    public OcifRepresentation setContent(String content) { this.content = content; return this; }
}
