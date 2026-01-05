package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.resource.IOcifResourceExtension;
import com.graphinout.reader.ocif.document.impl.OcifRepresentation;
import com.graphinout.reader.ocif.document.impl.OcifResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.input.ContentErrorException.contentWarn;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * OCIF Resource.
 * <p>
 * Spec excerpts (schema.json $defs.resource):
 * <ul>
 *   <li>id (string, required): A unique identifier for the resource.</li>
 *   <li>representations (array, required): A list of {@link OcifRepresentation}.</li>
 * </ul>
 * See spec.md section "Assets > Resources" for semantics and fallback behavior.
 */
public interface IOcifResource extends IOcifEntity {

    String TEXT_PLAIN = "text/plain";

    static @NonNull IOcifResourceMutable jsonToOcifResource(IJsonObject o, Consumer<ContentError> errorHandler) throws ContentErrorException {
        // obtain a usable resource id
        String id = Nullables.mapOrThrow(o.get(OCIF.Common.ID), IJsonValue::asString, () -> contentWarn("OCIF resource has no id"));
        IOcifResourceMutable res = new OcifResource(id);
        IJsonValue repsVal = o.get(OCIF.Resource.REPRESENTATIONS);
        if (repsVal != null && repsVal.isArray()) {
            IJsonArray reps = repsVal.asArray();
            for (int j = 0; j < reps.size(); j++) {
                IJsonObject repObj = reps.get_(j).asObject();
                // Either content or location must be given but not both
                String mimeType = repObj.getAsNonNullStringOrThrow(OCIF.Resource.MIME_TYPE, //
                        object -> ContentErrorException.contentError("OCIF representation has no mimeType"), //
                        value -> ContentErrorException.contentError("OCIF representation.mimeType is not a string but " + value.jsonType()));
                String location = repObj.getNullOrString(OCIF.Resource.LOCATION, //
                        value -> contentWarn("OCIF representation.location is not a string but " + value.jsonType()));
                String content = repObj.getNullOrString(OCIF.Resource.CONTENT, //
                        value -> contentWarn("OCIF representation.content is not a string but " + value.jsonType()));
                if (location == null && content == null) {
                    throw ContentErrorException.contentError("OCIF representation must have either 'location' or 'content'");
                }
                if (location != null && content != null) {
                    throw contentWarn("OCIF representation must have either 'location' or 'content' -- but not both");
                }

                IOcifRepresentation rep;
                if (content != null) {
                    rep = IOcifRepresentation.ofContent(content, mimeType);
                } else {
                    rep = IOcifRepresentation.ofLocation(location, mimeType);
                }

                res.addRepresentation(rep);
            }
        }
        return res;
    }

    static IOcifResourceMutable ofContentText(String id, String text) {
        IOcifResourceMutable r = new OcifResource(id); // id will be generated later
        r.addRepresentation(IOcifRepresentation.ofContent(text, TEXT_PLAIN));
        return r;
    }

    static IJsonValue resourceToJson(IOcifResource resource) {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(OCIF.Common.ID, resource.id());
        if (!resource.representations().isEmpty()) {
            IJsonArrayMutable representationsArray = factory().createArrayMutable();
            resource.representations().forEach(representation -> representationsArray.add(IOcifRepresentation.representationToJson(representation)));
            o.setProperty(OCIF.Resource.REPRESENTATIONS, representationsArray);
        }
        return o;
    }

    default Set<String> definedKeys() {
        return Set.of(OCIF.Common.ID, OCIF.Common.DATA, OCIF.Common.NODE);
    }

    /** Typed extensions parsed from the resource's data array. */
    @NonNull List<IOcifResourceExtension> extensions();

    /**
     *
     * @param mimeTypeQuery e.g. 'text' or 'text/plain'. Is used as a prefix query.
     * @return
     */
    default @Nullable IOcifRepresentation findRepresentationForMimeType(String mimeTypeQuery) {
        return representations().stream() //
                .filter(rep -> rep.matchesMimeType(mimeTypeQuery)) //
                .findFirst().orElse(null);
    }

    String id();

    /**
     *
     * @return true if representations are all text/plain
     */
    default boolean isAllRepresentationsAreTextPlain() {
        return representations().stream().allMatch(rep -> rep.matchesMimeType(TEXT_PLAIN));
    }

    List<IOcifRepresentation> representations();

}
