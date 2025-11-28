package com.graphinout.reader.ocif.document.extension;

/**
 * Marker interface for OCIF extensions (canvas, node, and relation extensions).
 * <p>
 * See spec: reader-ocif/src/spec-v0.6/spec.md, sections:
 * - Canvas Extensions
 * - Node Extensions
 * - Relation Extensions
 * </p>
 * Implementations generally expose the canonical OCIF extension name and URI
 * to assist with mapping and documentation.
 */
public interface IOcifExtension {
    /** Canonical extension name, e.g., "@ocif/node/rect". */
    String typeName();
    /** Canonical extension schema URI, e.g., "https://spec.canvasprotocol.org/v0.6/extensions/rect-node.json". */
    String typeUri();
}
