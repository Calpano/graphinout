package com.graphinout.reader.ocif;

import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.reader.ocif.document.IOcifDocument;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

public class OcifDoc2Json {

    public static IJsonValue toJsonValue(IOcifDocument ocifDocument) {
        IJsonObjectMutable root = JavaJsonFactory.INSTANCE.createObjectMutable();

        root.setProperty("ocif", JavaJsonFactory.INSTANCE.createString(ocifDocument.ocifSchemaURI()));

        if (!ocifDocument.canvasExtensions().isEmpty()) {
            IJsonArrayMutable extensionsArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            ocifDocument.canvasExtensions().forEach(extension -> extensionsArray.add(extensionToJson(extension)));
            root.setProperty("data", extensionsArray);
        }

        if (!ocifDocument.nodes().isEmpty()) {
            IJsonArrayMutable nodesArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            ocifDocument.nodes().forEach(node -> nodesArray.add(nodeToJson(node)));
            root.setProperty("nodes", nodesArray);
        }

        if (!ocifDocument.relations().isEmpty()) {
            IJsonArrayMutable relationsArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            ocifDocument.relations().forEach(relation -> relationsArray.add(relationToJson(relation)));
            root.setProperty("relations", relationsArray);
        }

        if (!ocifDocument.resources().isEmpty()) {
            IJsonArrayMutable resourcesArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            ocifDocument.resources().forEach(resource -> resourcesArray.add(resourceToJson(resource)));
            root.setProperty("resources", resourcesArray);
        }

        return root;
    }

    private static IJsonValue nodeToJson(IOcifNode node) {
        IJsonObjectMutable nodeJson = JavaJsonFactory.INSTANCE.createObjectMutable();
        nodeJson.setProperty("id", JavaJsonFactory.INSTANCE.createString(node.id()));
        if (node.position() != null) {
            IJsonArrayMutable posArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            for (double v : node.position()) {
                posArray.add(JavaJsonFactory.INSTANCE.createNumber(v));
            }
            nodeJson.setProperty("position", posArray);
        }
        if (node.size() != null) {
            IJsonArrayMutable sizeArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            for (double v : node.size()) {
                sizeArray.add(JavaJsonFactory.INSTANCE.createNumber(v));
            }
            nodeJson.setProperty("size", sizeArray);
        }
        if (node.rotation() != null) {
            nodeJson.setProperty("rotation", JavaJsonFactory.INSTANCE.createNumber(node.rotation()));
        }
        if (node.resource() != null) {
            nodeJson.setProperty("resource", JavaJsonFactory.INSTANCE.createString(node.resource()));
        }
        if (node.relation() != null) {
            nodeJson.setProperty("relation", JavaJsonFactory.INSTANCE.createString(node.relation()));
        }

        if (node.data() != null && !node.data().isEmpty()) {
            nodeJson.setProperty("data", node.data());
        }

        return nodeJson;
    }

    private static IJsonValue relationToJson(IOcifRelation relation) {
        IJsonObjectMutable relationJson = JavaJsonFactory.INSTANCE.createObjectMutable();
        relationJson.setProperty("id", JavaJsonFactory.INSTANCE.createString(relation.id()));
        if (relation.node() != null) {
            relationJson.setProperty("node", JavaJsonFactory.INSTANCE.createString(relation.node()));
        }

        if (!relation.extensions().isEmpty()) {
            IJsonArrayMutable extensionsArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            relation.extensions().forEach(extension -> extensionsArray.add(extensionToJson(extension)));
            relationJson.setProperty("data", extensionsArray);
        }
        return relationJson;
    }

    private static IJsonValue resourceToJson(IOcifResource resource) {
        IJsonObjectMutable resourceJson = JavaJsonFactory.INSTANCE.createObjectMutable();
        resourceJson.setProperty("id", JavaJsonFactory.INSTANCE.createString(resource.id()));
        if (!resource.representations().isEmpty()) {
            IJsonArrayMutable representationsArray = JavaJsonFactory.INSTANCE.createArrayMutable();
            resource.representations().forEach(representation -> representationsArray.add(representationToJson(representation)));
            resourceJson.setProperty("representations", representationsArray);
        }
        return resourceJson;
    }

    private static IJsonValue representationToJson(IOcifRepresentation representation) {
        IJsonObjectMutable representationJson = JavaJsonFactory.INSTANCE.createObjectMutable();
        representationJson.setProperty("mimeType", JavaJsonFactory.INSTANCE.createString(representation.mimeType()));
        if (representation.content() != null) {
            representationJson.setProperty("content", JavaJsonFactory.INSTANCE.createString(representation.content()));
        }
        if (representation.location() != null) {
            representationJson.setProperty("location", JavaJsonFactory.INSTANCE.createString(representation.location()));
        }
        return representationJson;
    }

    private static IJsonValue extensionToJson(IOcifExtension extension) {
        IJsonObjectMutable extensionJson = JavaJsonFactory.INSTANCE.createObjectMutable();
        extensionJson.setProperty("type", JavaJsonFactory.INSTANCE.createString(extension.typeUri()));
        if (extension.map() != null) {
            for (String key : extension.map().keySet()) {
                extensionJson.setProperty(key, extension.map().get(key));
            }
        }
        return extensionJson;
    }

    public static Object toJaJson(IOcifDocument ocifDocument) {
        return toJsonValue(ocifDocument).toJaJsonValue();
    }

    public static String toJsonString(IOcifDocument ocifDocument) {
        return toJsonValue(ocifDocument).toJsonString();
    }
}
