package com.graphinout.reader.ocif.todo;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;

public class OcifOutput {

    private final ICjDocument cjDoc;

    public OcifOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    /**
     * Convert {@link ICjDocument} to OCIF syntax. Reversing the mapping from reading OCIF to CJ, as specified in
     * {@link CjStream2OcifJson}.
     *
     * @return
     */
    public String toOcif() {
        IJsonFactory jsonFactory = IJsonFactory.INSTANCE;
        IJsonObjectMutable root = jsonFactory.createObjectMutable();

        // Document-level properties
        final boolean[] hasRelationsProperty = {false}; // Use an array to be modifiable from lambda
        if (cjDoc.data() != null && cjDoc.data().jsonValue() != null && cjDoc.data().jsonValue().isObject()) {
            cjDoc.data().jsonValue().asObject().getMaybeAs("ocif", IJsonValue::asObjectOrNull, (IJsonObject ocifData) -> {
                ocifData.getMaybe("schemaUri", schemaUri -> root.setProperty("ocif", schemaUri));
                ocifData.getMaybe("resources", resources -> root.setProperty("resources", resources));
                ocifData.getMaybe("schemas", schemas -> root.setProperty("schemas", schemas));
                ocifData.getMaybeAs("extra", IJsonValue::asObjectOrNull, (IJsonObject extras) -> {
                    extras.keys().forEach(k -> root.setProperty(k, extras.get(k)));
                });
                ocifData.getMaybeAs("flags", IJsonValue::asObjectOrNull, (IJsonObject flags) -> {
                    if (flags.hasProperty("hasRelationsProperty") && flags.get("hasRelationsProperty").asBoolean()) {
                        hasRelationsProperty[0] = true;
                    }
                });
            });
        }

        ICjGraph graph = cjDoc.theGraph();
        if (graph == null) {
            if (hasRelationsProperty[0]) {
                root.setProperty("relations", jsonFactory.createArrayMutable());
            }
            Json2StringWriter writer = new Json2StringWriter();
            root.fire(writer);
            return writer.jsonString();
        }

        // Nodes
        IJsonArrayMutable nodesArr = jsonFactory.createArrayMutable();
        for (ICjNode node : graph.nodes().toList()) {
            IJsonObjectMutable nodeObj = jsonFactory.createObjectMutable();
            if (node.id() != null) {
                nodeObj.setProperty("id", jsonFactory.createString(node.id()));
            }

            if (node.data() != null && node.data().jsonValue() != null && node.data().jsonValue().isObject()) {
                node.data().jsonValue().asObject().getMaybeAs("ocif", IJsonValue::asObjectOrNull, (IJsonObject ocifNodeData) -> {
                    ocifNodeData.getMaybeAs("node", IJsonValue::asObjectOrNull, (IJsonObject ocifNode) -> {
                        ocifNode.keys().forEach(key -> nodeObj.setProperty(key, ocifNode.get(key)));
                    });
                });
            }
            nodesArr.add(nodeObj);
        }
        root.setProperty("nodes", nodesArr);

        // Edges (Relations)
        boolean hasEdges = graph.edges().iterator().hasNext();

        if (hasEdges) {
            IJsonArrayMutable relationsArr = jsonFactory.createArrayMutable();
            for (ICjEdge edge : graph.edges().toList()) {
                if (edge.data() != null && edge.data().jsonValue() != null && edge.data().jsonValue().isObject()) {
                    edge.data().jsonValue().asObject().getMaybeAs("ocif", IJsonValue::asObjectOrNull, (IJsonObject ocifEdgeData) -> {
                        ocifEdgeData.getMaybe("relation", relationsArr::add);
                    });
                }
            }
            root.setProperty("relations", relationsArr);
        } else if (hasRelationsProperty[0]) {
            root.setProperty("relations", jsonFactory.createArrayMutable());
        }

        Json2StringWriter writer = new Json2StringWriter();
        root.fire(writer);
        return writer.jsonString();
    }

}
