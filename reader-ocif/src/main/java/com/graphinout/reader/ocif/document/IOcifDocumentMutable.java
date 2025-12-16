package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.IOcifExtension;

/**
 * OCIF Document API (object model).
 * <p>
 * Relevant spec excerpts:
 * <ul>
 *   <li>"An interchange file format for canvas-based applications. Visual nodes, structural relations, assets, and schemas." (spec.md, Abstract)</li>
 *   <li>"File Structure" (spec.md): An OCIF document is a JSON object with optional arrays: nodes, relations, resources, and schemas. The root MAY contain an "ocif" string with the schema TYPE_URI.</li>
 *   <li>See schema.json: Root properties include: ocif (string), nodes (array of node), relations (array of relation), resources (array of resource), schemas (array of schema).</li>
 * </ul>
 * This interface exposes a mutable in-memory model suitable for manipulation.
 */
public interface IOcifDocumentMutable extends IOcifDocument {

    IOcifDocumentMutable addCanvasExtension(IOcifExtension extension);

    IOcifDocumentMutable addNode(IOcifNode node);

    IOcifDocumentMutable addRelation(IOcifRelation relation);

    IOcifDocumentMutable addResource(IOcifResource resource);

    IOcifDocumentMutable addSchema(IOcifSchema schema);


    boolean removeNodeById(String id);

    boolean removeRelationById(String id);

    boolean removeResourceById(String id);

    void setOcifSchemaURI(String uri);

}
