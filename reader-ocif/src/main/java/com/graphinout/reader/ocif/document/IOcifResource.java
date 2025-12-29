package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.impl.OcifRepresentation;
import com.graphinout.reader.ocif.document.impl.OcifResource;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
public interface IOcifResource extends IOcifExtensibleEntity {

    String TEXT_PLAIN = "text/plain";

    static IOcifResource ofContentText(String id, String text) {
        IOcifResourceMutable r = new OcifResource(id); // id will be generated later
        r.addRepresentation(IOcifRepresentation.ofContent(text, TEXT_PLAIN));
        return r;
    }

    default Set<String> definedKeys() {
        return Set.of(OCIF.Common.ID, OCIF.Common.DATA, OCIF.Common.NODE);
    }

    /**
     *
     * @param mimeTypeQuery e.g. 'text' or 'text/plain'. Is used as a prefix query.
     * @return
     */
    default @Nullable IOcifRepresentation findRepresentationForMimeType(String mimeTypeQuery) {
        return representations().stream() //
                .filter(rep -> rep.mimeType() != null && rep.mimeType().toLowerCase().startsWith(mimeTypeQuery.toLowerCase())) //
                .findFirst().orElse(null);
    }

    String id();

    List<IOcifRepresentation> representations();

}
