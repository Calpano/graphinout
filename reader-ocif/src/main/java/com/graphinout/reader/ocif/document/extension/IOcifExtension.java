package com.graphinout.reader.ocif.document.extension;

import com.graphinout.reader.ocif.document.IOcifExtensibleEntity;
import com.graphinout.reader.ocif.document.IOcifSchema;
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
public interface IOcifExtension extends IOcifExtensibleEntity {

    default @Nullable IOcifSchema schema() {
        return IOcifSchema.of(typeUri());
    }

    /** Canonical extension name, e.g., "@ocif/node/rect". */
    @Nullable String typeName();

    /** Canonical extension schema TYPE_URI, e.g., "https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json". */
    @NonNull String typeUri();

    default @Nullable String version() {
        // FIXME can be different for imported, unknown ext
        return "0.6";
    }

}
