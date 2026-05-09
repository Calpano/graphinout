package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.document.extension.representation.IOcifRepresentationExtension;
import com.graphinout.reader.ocif07.document.impl.OcifRepresentation;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * OCIF Resource Representation.
 * <p>
 * Spec excerpts (schema.json $defs.representation): Either content or location MUST be present. If a data: TYPE_URI is
 * used in {@code location}, the content and MIME-type properties are implicitly defined already.
 */
public interface IOcifRepresentation extends IOcifEntity {

    static IOcifRepresentationMutable ofContent(String content, String mimeType) {
        return new OcifRepresentation(content, null, mimeType);
    }

    static IOcifRepresentationMutable ofLocation(String location, String mimeType) {
        return new OcifRepresentation(null, location, mimeType);
    }

    static IJsonValue representationToJson(IOcifRepresentation representation) {
        IJsonObjectMutable representationJson = factory().createObjectMutable();
        representationJson.setProperty("mimeType", factory().createString(representation.mimeType()));
        if (representation.content() != null) {
            representationJson.setProperty("content", factory().createString(representation.content()));
        }
        if (representation.location() != null) {
            representationJson.setProperty("location", factory().createString(representation.location()));
        }
        return representationJson;
    }

    /** content (string): Inline content (e.g., base64 data or text). */
    String content();

    @NonNull List<IOcifRepresentationExtension> extensions();

    /**
     * location (string): Storage location (relative/absolute TYPE_URI). If a data: TYPE_URI is used, content/mimeType
     * are implicit.
     */
    String location();

    /**
     * @param mimeTypeQuery case-insensitive prefix match
     */
    default boolean matchesMimeType(String mimeTypeQuery) {
        String mimeType = mimeType();
        return mimeType != null && mimeType.toLowerCase().startsWith(mimeTypeQuery.toLowerCase());
    }

    /** mimeType (string): IANA MIME Type of the content. */
    String mimeType();

}
