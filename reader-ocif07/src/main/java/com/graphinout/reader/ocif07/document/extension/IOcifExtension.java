package com.graphinout.reader.ocif07.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif07.document.IOcifSchema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Marker interface for OCIF extensions (canvas, node, and relation extensions).
 * <p>
 * See spec: reader-ocif/src/spec-v0.6/spec.md, sections:
 * <li>Canvas Extensions
 * <li>Node Extensions
 * <li>Relation Extensions
 * </p>
 * Implementations generally expose the canonical OCIF extension name and TYPE_URI to assist with mapping and
 * documentation.
 * <p>
 * It SHOULD have a version number, as part of its TYPE_URI. It SHOULD have a proposed name and SHOULD have a JSON
 * schema.
 */
public interface IOcifExtension {

    /** type URI */
    String TYPE = "type";

    /**
     * The JSON schema information of the extension
     */
    default @Nullable IOcifSchema schema() {
        return IOcifSchema.of(typeUri());
    }

    /**
     * @return never null, as we have the TYPE always present
     */
    @NonNull IJsonObject toJson();

    /** Canonical OCIF extension name, e.g., "@ocif/node/rect". */
    @Nullable String typeName();

    /**
     * Canonical extension schema TYPE_URI, e.g.,
     * {@code https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json}.
     */
    @NonNull String typeUri();

}
