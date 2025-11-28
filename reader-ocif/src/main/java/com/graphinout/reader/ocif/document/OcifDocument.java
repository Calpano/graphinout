package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * In-memory representation of an OCIF document.
 * <p>
 * Spec excerpts:
 * <ul>
 *   <li>spec.md — File Structure: Root object with optional arrays "nodes", "relations", "resources", and "schemas".</li>
 *   <li>schema.json — Root properties: ocif (string), nodes (array), relations (array), resources (array), schemas (array).</li>
 * </ul>
 */
public class OcifDocument implements IOcifDocument {

    private String ocifSchemaUri;
    private final List<OcifNode> nodes = new ArrayList<>();
    private final List<OcifRelation> relations = new ArrayList<>();
    private final List<OcifResource> resources = new ArrayList<>();
    private final List<OcifSchema> schemas = new ArrayList<>();
    private final List<IOcifExtension> canvasExtensions = new ArrayList<>();

    public OcifDocument() {}

    public OcifDocument(String ocifSchemaUri) {
        this.ocifSchemaUri = ocifSchemaUri;
    }

    @Override
    public String getOcifSchemaUri() {
        return ocifSchemaUri;
    }

    @Override
    public void setOcifSchemaUri(String uri) {
        this.ocifSchemaUri = uri;
    }

    @Override
    public List<OcifNode> getNodes() {
        return nodes;
    }

    @Override
    public List<OcifRelation> getRelations() {
        return relations;
    }

    @Override
    public List<OcifResource> getResources() {
        return resources;
    }

    @Override
    public List<OcifSchema> getSchemas() {
        return schemas;
    }

    @Override
    public List<IOcifExtension> getCanvasExtensions() { return canvasExtensions; }

    @Override
    public IOcifDocument addNode(OcifNode node) {
        nodes.add(Objects.requireNonNull(node));
        return this;
    }

    @Override
    public IOcifDocument addRelation(OcifRelation relation) {
        relations.add(Objects.requireNonNull(relation));
        return this;
    }

    @Override
    public IOcifDocument addResource(OcifResource resource) {
        resources.add(Objects.requireNonNull(resource));
        return this;
    }

    @Override
    public IOcifDocument addSchema(OcifSchema schema) {
        schemas.add(Objects.requireNonNull(schema));
        return this;
    }

    @Override
    public IOcifDocument addCanvasExtension(IOcifExtension extension) {
        canvasExtensions.add(Objects.requireNonNull(extension));
        return this;
    }

    @Override
    public boolean removeNodeById(String id) {
        if (id == null) return false;
        Iterator<OcifNode> it = nodes.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().getId())) { it.remove(); removed = true; }
        }
        return removed;
    }

    @Override
    public boolean removeRelationById(String id) {
        if (id == null) return false;
        Iterator<OcifRelation> it = relations.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().getId())) { it.remove(); removed = true; }
        }
        return removed;
    }

    @Override
    public boolean removeResourceById(String id) {
        if (id == null) return false;
        Iterator<OcifResource> it = resources.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().getId())) { it.remove(); removed = true; }
        }
        return removed;
    }
}
