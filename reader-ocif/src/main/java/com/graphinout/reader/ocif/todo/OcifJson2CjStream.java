package com.graphinout.reader.ocif.todo;

import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.of;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class OcifJson2CjStream {

    private static final Logger log = getLogger(OcifJson2CjStream.class);

    public static void parseOcifJsonObject2CjStream(IJsonObject ocifDoc, ICjStream cjStream, @Nullable Consumer<ContentError> errorHandler) {
        ICjDocumentChunkMutable doc = cjStream.createDocumentChunk();
        // Attach OCIF document-level data under data.ocif.* to keep roundtrip lossless (e.g., resources, schemas, ocif uri)
        doc.dataMutable(dm -> {
            // keep OCIF schema uri if present
            ifPresentAccept(ocifDoc.get(OCIF.Root.OCIF_SCHEMA_URI), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Schema.URI), v));
            ifPresentAccept(ocifDoc.get(OCIF.Root.RESOURCES), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Root.RESOURCES), v));
            ifPresentAccept(ocifDoc.get(OCIF.Root.SCHEMAS), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Root.SCHEMAS), v));
            // Record if input explicitly had a relations property (even if empty)
            if (ocifDoc.hasProperty(OCIF.Root.RELATIONS)) {
                dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Root.FLAGS, com.graphinout.reader.ocif.OCIF.Root.HAS_RELATIONS_PROPERTY), dm.factory().createBoolean(true));
            }
            // any other root-level extras not mapped go to ocif.extra
            IJsonObjectMutable extra = dm.factory().createObjectMutable();
            Set<String> knownRootKeys = new HashSet<>(Arrays.asList(OCIF.Root.NODES, com.graphinout.reader.ocif.OCIF.Root.RELATIONS, com.graphinout.reader.ocif.OCIF.Root.RESOURCES, com.graphinout.reader.ocif.OCIF.Root.SCHEMAS, OCIF.Root.OCIF_SCHEMA_URI));
            for (String key : ocifDoc.keys()) {
                if (!knownRootKeys.contains(key)) {
                    extra.setProperty(key, ocifDoc.get(key));
                }
            }
            if (!extra.isEmpty()) {
                dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Root.EXTRA), extra);
            }
        });
        cjStream.documentStart(doc);

        // Create single graph to hold nodes/relations
        ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        // Nodes
        IJsonValue ocifNodes = ocifDoc.get(OCIF.Root.NODES);
        if (ocifNodes != null && ocifNodes.isArray()) {
            ocifNodes.asArray().forEach(jsonNode->{
                IJsonObject ocifNode = jsonNode.asObject();
                cjStream.node( cjNode-> toCjNode(ocifNode, cjNode));
            });
        }

        // Relations -> edges
        IJsonValue ocifRelations = ocifDoc.get(OCIF.Root.RELATIONS);
        if (ocifRelations != null && ocifRelations.isArray()) {
            ocifRelations.asArray().forEach(jsonRelation -> {
                IJsonObject ocifRelation = jsonRelation.asObject();
                cjStream.edge(cjEdge -> toCjEdge(ocifRelation, cjEdge));
            });
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    private static void toCjEdge(IJsonObject rel, ICjEdgeChunkMutable edge) {
        // endpoints depend on relation type
        // TODO what is the correct default type?
        String type = Nullables.nonNullOrDefault(rel.get(OCIF.Common.TYPE), IJsonValue::asString, com.graphinout.reader.ocif.OCIF.Type.OCIF_REL_EDGE);
        switch (type) {
            case OCIF.Type.OCIF_REL_PARENT_CHILD -> {
                String parent = requireNonNull(rel.get(OCIF.Common.PARENT)).asString();
                String child = requireNonNull(rel.get(OCIF.Common.CHILD)).asString();
                edge.addEndpoint(ep -> ep.node(parent));
                edge.addEndpoint(ep -> ep.node(child));
            }
            case OCIF.Type.OCIF_REL_EDGE -> {
                // endpoints array with from/to
                if (rel.hasProperty(OCIF.Common.FROM) && rel.hasProperty(com.graphinout.reader.ocif.OCIF.Common.TO)) {
                    edge.addEndpoint(ep -> ep.node(rel.get_(OCIF.Common.FROM).asString()));
                    edge.addEndpoint(ep -> ep.node(rel.get_(OCIF.Common.TO).asString()));
                }
            }
            case OCIF.Type.OCIF_REL_HYPEREDGE -> {
                IJsonValue epsVal = rel.get(OCIF.Common.ENDPOINTS);
                if (epsVal != null && epsVal.isArray()) {
                    IJsonArray eps = epsVal.asArray();
                    for (int j = 0; j < eps.size(); j++) {
                        IJsonObject eo = eps.get_(j).asObject();
                        edge.addEndpoint(ep -> {
                            ifPresentAccept(eo.get(OCIF.Common.NODE), IJsonValue::asString, ep::node);
                            ifPresentAccept(eo.get(OCIF.Common.PORT), IJsonValue::asString, ep::port);
                        });
                    }
                }
            }
            default -> log.warn("Unknown relation type '{}'", type);
        }
        // carry relation fields into edge.data.ocif.relation
        edge.dataMutable(dm -> {
            IJsonContainerNavigationStep ocif = of(OCIF.Root.OCIF_SCHEMA_URI);
            IJsonContainerNavigationStep relStep = of(OCIF.Node.RELATION);
            for (String rk : rel.keys()) {
                dm.add(List.of(ocif, relStep, of(rk)), rel.get(rk));
            }
        });
    }

    private static void toCjNode(IJsonObject nodeObj, ICjNodeChunkMutable node) {
        ifPresentAccept(nodeObj.get(OCIF.Common.ID), IJsonValue::asString, node::id);

        // map known OCIF node fields under data.ocif.node.* to keep structure
        node.dataMutable(dm -> {
            ifPresentAccept(nodeObj.get(OCIF.Node.POSITION), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, OCIF.Node.POSITION), v));
            ifPresentAccept(nodeObj.get(OCIF.Node.SIZE), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, OCIF.Node.SIZE), v));
            ifPresentAccept(nodeObj.get(OCIF.Node.RESOURCE), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, OCIF.Node.RESOURCE), v));
            ifPresentAccept(nodeObj.get(OCIF.Common.TYPE), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, com.graphinout.reader.ocif.OCIF.Common.TYPE), v));
            ifPresentAccept(nodeObj.get(OCIF.Common.DATA), v -> //
                    dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, com.graphinout.reader.ocif.OCIF.Common.DATA), v));
            // preserve any unknown fields
            IJsonObjectMutable extras = dm.factory().createObjectMutable();
            Set<String> knownNodeKeys = new HashSet<>(Arrays.asList(OCIF.Common.ID, OCIF.Node.POSITION, OCIF.Node.SIZE, OCIF.Node.RESOURCE, com.graphinout.reader.ocif.OCIF.Common.TYPE, com.graphinout.reader.ocif.OCIF.Common.DATA));
            for (String nk : nodeObj.keys()) {
                if (!knownNodeKeys.contains(nk)) {
                    extras.setProperty(nk, nodeObj.get(nk));
                }
            }
            if (!extras.isEmpty()) {
                dm.add(pathOf(OCIF.Root.OCIF_SCHEMA_URI, com.graphinout.reader.ocif.OCIF.Common.NODE, com.graphinout.reader.ocif.OCIF.Root.EXTRA), extras);
            }
        });
    }

}
