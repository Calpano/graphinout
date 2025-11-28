package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.List;
import java.util.Optional;

/**
 * OCIF Document API (object model).
 * <p>
 * Relevant spec excerpts:
 * <ul>
 *   <li>"An interchange file format for canvas-based applications. Visual nodes, structural relations, assets, and schemas." (spec.md, Abstract)</li>
 *   <li>"File Structure" (spec.md): An OCIF document is a JSON object with optional arrays: nodes, relations, resources, and schemas. The root MAY contain an "ocif" string with the schema URI.</li>
 *   <li>See schema.json: Root properties include: ocif (string), nodes (array of node), relations (array of relation), resources (array of resource), schemas (array of schema).</li>
 * </ul>
 * This interface exposes a mutable in-memory model suitable for manipulation.
 */
public interface IOcifDocument {

    /** URI of the OCIF schema (root property "ocif"). */
    String getOcifSchemaUri();
    void setOcifSchemaUri(String uri);

    List<OcifNode> getNodes();
    List<OcifRelation> getRelations();
    List<OcifResource> getResources();
    List<OcifSchema> getSchemas();
    /** Canvas-level extensions, e.g., viewport. */
    List<IOcifExtension> getCanvasExtensions();

    // Convenience
    default Optional<OcifNode> findNode(String id) {
        if (id == null) return Optional.empty();
        return getNodes().stream().filter(n -> id.equals(n.getId())).findFirst();
    }

    default Optional<OcifRelation> findRelation(String id) {
        if (id == null) return Optional.empty();
        return getRelations().stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    default Optional<OcifResource> findResource(String id) {
        if (id == null) return Optional.empty();
        return getResources().stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    // Mutators
    IOcifDocument addNode(OcifNode node);
    IOcifDocument addRelation(OcifRelation relation);
    IOcifDocument addResource(OcifResource resource);
    IOcifDocument addSchema(OcifSchema schema);
    IOcifDocument addCanvasExtension(IOcifExtension extension);

    boolean removeNodeById(String id);
    boolean removeRelationById(String id);
    boolean removeResourceById(String id);
}
