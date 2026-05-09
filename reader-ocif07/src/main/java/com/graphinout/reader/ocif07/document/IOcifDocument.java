package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.canvas.IOcifCanvasExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.graphinout.reader.ocif07.Ocifs.factory;

public interface IOcifDocument {

    static IJsonValue toJsonValue(IOcifDocument ocifDocument) {
        IJsonObjectMutable root = factory().createObjectMutable();

        root.setProperty(OCIF._OCIF_, factory().createString(ocifDocument.ocifSchemaURI()));

        if (!ocifDocument.canvasExtensions().isEmpty()) {
            IJsonArrayMutable extensionsArray = factory().createArrayMutable();
            ocifDocument.canvasExtensions().forEach(extension -> extensionsArray.add(extension.toJson()));
            root.setProperty(OCIF.Common.DATA, extensionsArray);
        }

        if (!ocifDocument.nodes().isEmpty()) {
            IJsonArrayMutable nodesArray = factory().createArrayMutable();
            ocifDocument.nodes().forEach(node -> nodesArray.add(IOcifNode.nodeToJson(node)));
            root.setProperty(OCIF.Root.NODES, nodesArray);
        }

        if (!ocifDocument.relations().isEmpty()) {
            IJsonArrayMutable relationsArray = factory().createArrayMutable();
            ocifDocument.relations().forEach(relation -> relationsArray.add(IOcifRelation.relationToJson(relation)));
            root.setProperty(OCIF.Root.RELATIONS, relationsArray);
        }

        if (!ocifDocument.resources().isEmpty()) {
            IJsonArrayMutable resourcesArray = factory().createArrayMutable();
            ocifDocument.resources().forEach(resource -> resourcesArray.add(IOcifResource.resourceToJson(resource)));
            root.setProperty(OCIF.Root.RESOURCES, resourcesArray);
        }

        if (!ocifDocument.schemas().isEmpty()) {
            IJsonArrayMutable schemasArray = factory().createArrayMutable();
            ocifDocument.schemas().forEach(schema -> schemasArray.add(IOcifSchema.schemaToJson(schema)));
            root.setProperty(OCIF.Root.SCHEMAS, schemasArray);
        }

        return root;
    }

    /** Canvas-level extensions, e.g., viewport. */
    List<IOcifCanvasExtension> canvasExtensions();

    default Optional<IOcifNode> findNode(String id) {
        if (id == null) return Optional.empty();
        return nodes().stream().filter(n -> id.equals(n.id())).findFirst();
    }

    default Optional<IOcifRelation> findRelation(String id) {
        if (id == null) return Optional.empty();
        return relations().stream().filter(r -> id.equals(r.id())).findFirst();
    }

    default Optional<IOcifResource> findResource(String id) {
        if (id == null) return Optional.empty();
        return resources().stream().filter(r -> id.equals(r.id())).findFirst();
    }

    /** Mutable nodes list */
    List<IOcifNode> nodes();

    /** TYPE_URI of the OCIF schema (root property "ocif"). */
    @NonNull String ocifSchemaURI();

    /** Mutable relations list */
    List<IOcifRelation> relations();

    default @Nullable IOcifResource resourceById(@NonNull String resourceId) {
        return resources().stream().filter(r -> resourceId.equals(r.id())).findFirst().orElse(null);
    }

    /** Mutable resources list */
    List<IOcifResource> resources();

    @Nullable IOcifNode rootNode();

    /** Mutable schemas list */
    List<IOcifSchema> schemas();

}
