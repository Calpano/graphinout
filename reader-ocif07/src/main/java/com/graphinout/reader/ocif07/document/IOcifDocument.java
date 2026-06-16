package com.graphinout.reader.ocif07.document;

import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.extension.canvas.IOcifCanvasExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        // v0.7.1: a resource referenced by exactly one node is emitted inline on that node (and omitted from the
        // top-level "resources" array). Resources shared by several nodes — or referenced by none — stay top-level.
        Map<String, Integer> resourceRefCounts = new HashMap<>();
        for (IOcifNode node : ocifDocument.nodes()) {
            String resourceId = node.resource();
            if (resourceId != null) {
                resourceRefCounts.merge(resourceId, 1, Integer::sum);
            }
        }
        Set<String> inlinedResourceIds = new HashSet<>();

        if (!ocifDocument.nodes().isEmpty()) {
            IJsonArrayMutable nodesArray = factory().createArrayMutable();
            for (IOcifNode node : ocifDocument.nodes()) {
                IJsonValue nodeJson = IOcifNode.nodeToJson(node);
                String resourceId = node.resource();
                if (resourceId != null && resourceRefCounts.getOrDefault(resourceId, 0) == 1
                        && nodeJson instanceof IJsonObjectMutable nodeObj) {
                    IOcifResource resource = ocifDocument.findResource(resourceId).orElse(null);
                    if (resource != null && IOcifResource.resourceToJson(resource) instanceof IJsonObjectMutable inlineResource) {
                        inlineResource.removeProperty(OCIF.Common.ID); // an inline resource has no id
                        nodeObj.setProperty(OCIF.Node.RESOURCE, inlineResource);
                        inlinedResourceIds.add(resourceId);
                    }
                }
                nodesArray.add(nodeJson);
            }
            root.setProperty(OCIF.Root.NODES, nodesArray);
        }

        if (!ocifDocument.relations().isEmpty()) {
            IJsonArrayMutable relationsArray = factory().createArrayMutable();
            ocifDocument.relations().forEach(relation -> relationsArray.add(IOcifRelation.relationToJson(relation)));
            root.setProperty(OCIF.Root.RELATIONS, relationsArray);
        }

        List<IOcifResource> topLevelResources = ocifDocument.resources().stream()
                .filter(resource -> !inlinedResourceIds.contains(resource.id()))
                .toList();
        if (!topLevelResources.isEmpty()) {
            IJsonArrayMutable resourcesArray = factory().createArrayMutable();
            topLevelResources.forEach(resource -> resourcesArray.add(IOcifResource.resourceToJson(resource)));
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
