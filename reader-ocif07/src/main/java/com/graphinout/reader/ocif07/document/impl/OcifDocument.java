package com.graphinout.reader.ocif07.document.impl;

import com.graphinout.foundation.pure.collections.IdFactory;
import com.graphinout.reader.ocif07.document.IOcifDocumentMutable;
import com.graphinout.reader.ocif07.document.IOcifNode;
import com.graphinout.reader.ocif07.document.IOcifRelation;
import com.graphinout.reader.ocif07.document.IOcifRepresentation;
import com.graphinout.reader.ocif07.document.IOcifResource;
import com.graphinout.reader.ocif07.document.IOcifResourceMutable;
import com.graphinout.reader.ocif07.document.IOcifSchema;
import com.graphinout.reader.ocif07.document.extension.canvas.IOcifCanvasExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
public class OcifDocument implements IOcifDocumentMutable {

    private final IdFactory idFactory = IdFactory.createCounting("a");
    private final List<IOcifNode> nodes = new ArrayList<>();
    private final List<IOcifRelation> relations = new ArrayList<>();
    private final List<IOcifResource> resources = new ArrayList<>();
    private final List<IOcifSchema> schemas = new ArrayList<>();
    private final List<IOcifCanvasExtension> canvasExtensions = new ArrayList<>();
    private String ocifSchemaURI;
    private @Nullable String rootNodeId;

    public OcifDocument() {}

    public OcifDocument(@NonNull String ocifSchemaURI) {
        this.ocifSchemaURI = ocifSchemaURI;
    }

    @Override
    public IOcifDocumentMutable addCanvasExtension(IOcifCanvasExtension extension) {
        canvasExtensions.add(Objects.requireNonNull(extension));
        return this;
    }

    @Override
    public IOcifDocumentMutable addNode(@NonNull IOcifNode node) {
        nodes.add(node);
        return this;
    }

    @Override
    public IOcifDocumentMutable addRelation(IOcifRelation relation) {
        relations.add(Objects.requireNonNull(relation));
        return this;
    }

@Override
    public IOcifDocumentMutable addResource(IOcifResource resource) {
        resources.add(Objects.requireNonNull(resource));
        return this;
    }

    @Override
    public IOcifDocumentMutable addSchema(IOcifSchema schema) {
        schemas.add(Objects.requireNonNull(schema));
        return this;
    }

    @Override
    public void rootNodeId(@NonNull String rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public String createId() {
        return idFactory.createId();
    }

    public IOcifResourceMutable createTextResource(String text) {
        IOcifResourceMutable resource = new OcifResource(createId());
        resource.addRepresentation(IOcifRepresentation.ofContent(text, IOcifResourceMutable.TEXT_PLAIN));
        return resource;
    }

    @Override
    public List<IOcifCanvasExtension> canvasExtensions() {return canvasExtensions;}

    @Override
    public @NonNull String ocifSchemaURI() {
        return ocifSchemaURI;
    }

    @Override
    public @Nullable IOcifNode rootNode() {
        if(rootNodeId == null)
            return null;
        return nodes.stream()
                .filter(node -> node.id().equals(rootNodeId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<IOcifRelation> relations() {
        return relations;
    }

    @Override
    public List<IOcifResource> resources() {
        return resources;
    }

    @Override
    public List<IOcifSchema> schemas() {
        return schemas;
    }

    @Override
    public List<IOcifNode> nodes() {
        return nodes;
    }

    @Override
    public boolean removeNodeById(String id) {
        if (id == null) return false;
        Iterator<IOcifNode> it = nodes.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().id())) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public boolean removeRelationById(String id) {
        if (id == null) return false;
        Iterator<IOcifRelation> it = relations.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().id())) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public boolean removeResourceById(String id) {
        if (id == null) return false;
        Iterator<IOcifResource> it = resources.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (id.equals(it.next().id())) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public void ocifSchemaURI(String uri) {
        this.ocifSchemaURI = uri;
    }


}
