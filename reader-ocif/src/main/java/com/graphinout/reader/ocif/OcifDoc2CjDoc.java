package com.graphinout.reader.ocif;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjEdgeTypeSource;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.JsonTypes;
import com.graphinout.foundation.pure.json.value.JsonValues;
import com.graphinout.foundation.pure.value.BooleanRef;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.HyperedgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;

import java.util.Objects;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;

public class OcifDoc2CjDoc {


    private static void importPortsRecursive(ICjNodeMutable node, com.graphinout.foundation.pure.json.document.IJsonArray ports) {
        for (int i = 0; i < ports.size(); i++) {
            var pobj = ports.get_(i).asObject();
            String id = pobj.getString("id");
            node.addPort(pm -> {
                if (id != null) pm.id(id);
                var data = pobj.get("data");
                if (data != null && data.isObject()) {
                    var o = data.asObject();
                    pm.dataMutable(d -> //
                            o.keys().forEach(k -> {
                                var v = o.get(k);
                                if (v != null) d.add(k, v);
                            }));
                }
                var children = pobj.get("ports");
                if (children != null && children.isArray()) {
                    importSubPorts(pm, children.asArray());
                }
            });
        }
    }

    private static void importSubPorts(com.graphinout.base.cj.document.ICjPortMutable parent, com.graphinout.foundation.pure.json.document.IJsonArray ports) {
        for (int i = 0; i < ports.size(); i++) {
            var pobj = ports.get_(i).asObject();
            String id = pobj.getString("id");
            parent.addPort(pm -> {
                if (id != null) pm.id(id);
                var data = pobj.get("data");
                if (data != null && data.isObject()) {
                    var o = data.asObject();
                    pm.dataMutable(d -> //
                            o.keys().forEach(k -> {
                                var v = o.get(k);
                                if (v != null) d.add(k, v);
                            }));
                }
                var children = pobj.get("ports");
                if (children != null && children.isArray()) {
                    importSubPorts(pm, children.asArray());
                }
            });
        }
    }

    private static void ocifDocument2cjGraph(OcifDocument ocifDocument, ICjGraphMutable cjGraph) {
        for (IOcifNode ocifNode : ocifDocument.nodes()) {
            cjGraph.addNode(cjNode -> ocifNode2cjNode(ocifDocument, ocifNode, cjNode));
        }
        for (IOcifRelation ocifRelation : ocifDocument.relations()) {
            cjGraph.addEdge(cjEdge -> ocifRelation2cjEdge(ocifRelation, cjEdge));
        }
    }

    private static void ocifNode2cjNode(OcifDocument ocifDocument, IOcifNode ocifNode, ICjNodeMutable cjNode) {
        cjNode.id(ocifNode.id());
        cjNode.dataMutable(d -> {
            ifPresentAccept(ocifNode.position(), v -> d.add(OCIF.Node.POSITION, v.toJson()));
            ifPresentAccept(ocifNode.size(), v -> d.add(OCIF.Node.SIZE, v.toJson()));
            ifPresentAccept(ocifNode.resourceFit(), v -> d.add(OCIF.Node.RESOURCE_FIT, v.name()));
            ifPresentAccept(ocifNode.rotation(), v -> d.add(OCIF.Node.ROTATION, v));
            ifPresentAccept(ocifNode.relation(), v -> d.add(OCIF.Node.RELATION, v));
            ifPresentAccept(ocifNode.resource(), v -> d.add(OCIF.Node.RESOURCE, v));
        });

        var unknownExts = IJsonFactory.INSTANCE.createArrayMutable();
        for(IOcifExtension ocifExt : ocifNode.extensions()) {

            switch (ocifExt) {
                case DataExtension data -> {
                    // TODO some data is just copied, some was stored by CjDoc2OcifDoc for round-tripping
                    DataExtension data2 = data.copy();
                    unknownExts.add(OcifDoc2Json.extensionToJson(ocifExt));
                }
                default -> unknownExts.add(OcifDoc2Json.extensionToJson(ocifExt));
            }

            if (ocifExt instanceof DataExtension ocifData) {
                IJsonValue labelsVal = ocifData.map().get("cj:labels");
                if (labelsVal != null && labelsVal.isArray()) {
                    var arr = labelsVal.asArray();
                    for (int i = 0; i < arr.size(); i++) {
                        var o = arr.get_(i).asObject();
                        String value = o.getString(CjConstants.VALUE);
                        String lang = o.getString(CjConstants.LANGUAGE);
                        if (value != null) {
                            // FIXME labelsAddedFromExt[0] = true;
                            if (lang == null) cjNode.addLabelWithoutLanguage(value);
                            else cjNode.addLabel(value, lang);
                        }
                    }
                }
                // Ports import
                IJsonValue portsVal = ocifData.map().get("cj:ports");
                if (portsVal != null && portsVal.isArray()) {
                    importPortsRecursive(cjNode, portsVal.asArray());
                }
                // Node data import
                IJsonValue dataVal = ocifData.map().get("cj:data");
                if (dataVal != null && dataVal.isObject()) {
                    var obj = dataVal.asObject();
                    cjNode.dataMutable(d -> //
                            obj.keys().forEach(k -> {
                                var v = obj.get(k);
                                if (v != null) d.add(k, v);
                            }));
                }
                // Filter out cj:* keys and see if anything else remains in DataExtension
                var remaining = IJsonFactory.INSTANCE.createObjectMutable();
                ocifData.map().forEach((k, v) -> {
                    if (!k.equals("cj:labels") && !k.equals("cj:ports") && !k.equals("cj:data")) {
                        remaining.setProperty(k, v);
                    }
                });
                if (!remaining.isEmpty()) {
                    unknownExts.add(OcifDoc2Json.extensionToJson(ocifData));
                }
            } else {
                unknownExts.add(OcifDoc2Json.extensionToJson(ocifExt));
            }


        }

        ifPresentAccept(ocifNode.extensions(), exts -> {
            var labelsAddedFromExt = new boolean[]{false};
            for (IOcifExtension ext : exts) {
                if (ext instanceof DataExtension ocifData) {
                    IJsonValue labelsVal = ocifData.map().get("cj:labels");
                    if (labelsVal != null && labelsVal.isArray()) {
                        var arr = labelsVal.asArray();
                        for (int i = 0; i < arr.size(); i++) {
                            var o = arr.get_(i).asObject();
                            String value = o.getString(CjConstants.VALUE);
                            String lang = o.getString(CjConstants.LANGUAGE);
                            if (value != null) {
                                labelsAddedFromExt[0] = true;
                                if (lang == null) cjNode.addLabelWithoutLanguage(value);
                                else cjNode.addLabel(value, lang);
                            }
                        }
                    }
                    // Ports import
                    IJsonValue portsVal = ocifData.map().get("cj:ports");
                    if (portsVal != null && portsVal.isArray()) {
                        importPortsRecursive(cjNode, portsVal.asArray());
                    }
                    // Node data import
                    IJsonValue dataVal = ocifData.map().get("cj:data");
                    if (dataVal != null && dataVal.isObject()) {
                        var obj = dataVal.asObject();
                        cjNode.dataMutable(d -> //
                                obj.keys().forEach(k -> {
                                    var v = obj.get(k);
                                    if (v != null) d.add(k, v);
                                }));
                    }
                    // Filter out cj:* keys and see if anything else remains in DataExtension
                    var remaining = IJsonFactory.INSTANCE.createObjectMutable();
                    ocifData.map().forEach((k, v) -> {
                        if (!k.equals("cj:labels") && !k.equals("cj:ports") && !k.equals("cj:data")) {
                            remaining.setProperty(k, v);
                        }
                    });
                    if (!remaining.isEmpty()) {
                        unknownExts.add(OcifDoc2Json.extensionToJson(ocifData));
                    }
                } else {
                    unknownExts.add(OcifDoc2Json.extensionToJson(ext));
                }
            }
            if (!unknownExts.isEmpty()) {
                cjNode.dataMutable(d -> d.add("cj:ocifExtensions", unknownExts));
            }

            BooleanRef labelAddedFromResource = BooleanRef.FALSE();
            // Reconstruct a simple CJ label from linked OCIF resource only if not already covered by extension
            if (!labelsAddedFromExt[0]) {

                ifPresentAccept(ocifNode.resource(), resId -> {
                    IOcifResource res = ocifDocument.findResource(resId).orElse(null);
                    if (res == null) {
                        throw new IllegalStateException("OCIF node '" + ocifNode.id() +
                                "' references resource '" + resId + "', which was not found.");
                    }
                    if (res.representations() == null || res.representations().isEmpty()) {
                        throw new IllegalStateException("OCIF resource '" + resId + "' has no representations.");
                    }
                    IOcifRepresentation rep = res.findRepresentationForMimeType(IOcifResource.TEXT_PLAIN);
                    if (rep == null) {
                            // TODO remember resource id in cj:data
                            ocifRepresentation2cjLabel(rep, cjNode);
                            // TODO remember this resource is ported
                    }

// FIXME
//                    var rep = res.representations().getFirst();
//                    if (rep.content() != null && (rep.mimeType() == null || IOcifResource.TEXT_PLAIN.equals(rep.mimeType()))) {
//                        String lang = null;
//                        var langVal = res.map().get(CjConstants.LANGUAGE);
//                        if (langVal != null && langVal.isString()) {
//                            lang = langVal.asString();
//                        }
//                        if (lang == null) {
//                            cjNode.addLabelWithoutLanguage(rep.content());
//                        } else {
//                            cjNode.addLabel(rep.content(), lang);
//                        }
//                        labelAddedFromResource.set(true);
//                    }
                });
            }
            // Only persist raw resource reference in CJ data if it wasn't used to materialize labels
            if (!labelsAddedFromExt[0] && !labelAddedFromResource.get()) {
                ifPresentAccept(ocifNode.resource(), r -> cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.RESOURCE), r)));
            }
        });
    }

    private static void ocifRelation2cjEdge(IOcifRelation ocifRelation, ICjEdgeMutable cjEdge) {
        // Preserve relation/edge ID only if not auto-generated (a*)
        if (ocifRelation.id() != null && !ocifRelation.id().matches("a\\d+")) {
            cjEdge.id(ocifRelation.id());
        }
        var unknownExts = IJsonFactory.INSTANCE.createArrayMutable();
        for (IOcifExtension ext : ocifRelation.extensions()) {
            switch (ext) {
                case DataExtension ocifData -> {
                    // Interpret known mapping keys (do not copy mapping metadata into CJ data)
                    IJsonValue labelsVal = ocifData.map().get("cj:labels");
                    if (labelsVal != null && labelsVal.isArray()) {
                        var arr = labelsVal.asArray();
                        for (int i = 0; i < arr.size(); i++) {
                            var o = arr.get_(i).asObject();
                            String value = o.getString("value");
                            String lang = o.getString(CjConstants.LANGUAGE);
                            if (value != null) {
                                cjEdge.addLabel(value, lang);
                            }
                        }
                    }
                    // Filter out cj:* keys and see if anything else remains in DataExtension
                    var remaining = IJsonFactory.INSTANCE.createObjectMutable();
                    ocifData.map().forEach((k, v) -> {
                        if (!k.equals("cj:labels")) {
                            remaining.setProperty(k, v);
                        }
                    });
                    if (!remaining.isEmpty()) {
                        unknownExts.add(OcifDoc2Json.extensionToJson(ocifData));
                    }
                }
                case EdgeRelationExtension edge -> {
                    assert Objects.equals(edge.typeName(), OCIF.Type.OCIF_REL_EDGE);
                    String startId = edge.start();
                    String endId = edge.end();
                    String[] s = splitEndpoint(startId);
                    String[] t = splitEndpoint(endId);
                    if (edge.directed()) {
                        cjEdge.addEndpoint(ep -> {
                            ep.node(s[0]);
                            if (s[1] != null) ep.port(s[1]);
                            ep.direction(CjDirection.IN);
                        });
                        cjEdge.addEndpoint(ep -> {
                            ep.node(t[0]);
                            if (t[1] != null) ep.port(t[1]);
                            ep.direction(CjDirection.OUT);
                        });
                    } else {
                        cjEdge.addEndpoint(ep -> {
                            ep.node(s[0]);
                            if (s[1] != null) ep.port(s[1]);
                            ep.direction(CjDirection.UNDIR);
                        });
                        cjEdge.addEndpoint(ep -> {
                            ep.node(t[0]);
                            if (t[1] != null) ep.port(t[1]);
                            ep.direction(CjDirection.UNDIR);
                        });
                    }
                    ifPresentAccept(edge.rel(), rel -> {
                        // TODO rel might be a TYPE_URI
                        cjEdge.edgeType(ICjEdgeType.of(CjEdgeTypeSource.String, rel));
                    });
                    ifPresentAccept(edge.node(), nodeId -> //
                            cjEdge.dataMutable(data -> data.add(OCIF.Common.NODE, nodeId)));
                }
                case HyperedgeRelationExtension hex -> {
                    // map all endpoints as undirected endpoints; preserve rel as edgeType
                    for (var ep : hex.endpoints()) {
                        String[] st = splitEndpoint(ep.id());
                        CjDirection dir = switch (String.valueOf(ep.direction())) {
                            case "in" -> CjDirection.IN;
                            case "out" -> CjDirection.OUT;
                            default -> CjDirection.UNDIR;
                        };
                        cjEdge.addEndpoint(e -> {
                            e.node(st[0]);
                            if (st[1] != null) e.port(st[1]);
                            e.direction(dir);
                        });
                    }
                    if (hex.rel() != null) {
                        cjEdge.edgeType(ICjEdgeType.of(CjEdgeTypeSource.String, hex.rel()));
                    }
                }
                default -> unknownExts.add(OcifDoc2Json.extensionToJson(ext));
            }
        }
        if (!unknownExts.isEmpty()) {
            cjEdge.dataMutable(d -> d.add("cj:ocifExtensions", unknownExts));
        }
    }

    private static void ocifRepresentation2cjLabel(IOcifRepresentation rep, ICjHasLabelMutable hasLabelMutable) {
        assert rep.content() != null;
        assert rep.mimeType() != null;
        hasLabelMutable.labelMutable(cjLabel -> cjLabel.addEntry(entry->{
            entry.value(rep.content());
            // language of representation is in OCIF extensions of that representation
            ifPresentAccept(mapOrNull(rep.map().get(CjConstants.LANGUAGE), IJsonValue::asString), entry::language);
        }));
    }

    private static String[] splitEndpoint(String id) {
        if (id == null) return new String[]{null, null};
        int idx = id.indexOf('#');
        if (idx < 0) return new String[]{id, null};
        String node = id.substring(0, idx);
        String port = id.substring(idx + 1);
        if (port.isEmpty()) port = null;
        return new String[]{node, port};
    }

    public static ICjDocument toCjDocument(OcifDocument ocifDocument) {
        ICjDocumentMutable cjDocument = new CjDocumentElement();

        // Preserve OCIF schema URI
        if (ocifDocument.ocifSchemaURI() != null) {
            cjDocument.dataMutable(d -> d.add("cj:ocifSchema", IJsonFactory.INSTANCE.createString(ocifDocument.ocifSchemaURI())));
        }

        // Preserve OCIF resources
        if (!ocifDocument.resources().isEmpty()) {
            var arr = IJsonFactory.INSTANCE.createArrayMutable();
            ocifDocument.resources().forEach(res -> arr.add(OcifDoc2Json.resourceToJson(res)));
            cjDocument.dataMutable(d -> d.add("cj:ocifResources", arr));
        }

        // Import document-level baseUri from canvas extension
        ocifDocument.canvasExtensions().stream().filter(e -> e instanceof DataExtension).map(e -> (DataExtension) e).map(e -> e.map().get("cj:baseUri")).filter(v -> v != null && v.isString()).findFirst().ifPresent(baseUriExt -> cjDocument.baseUri(baseUriExt.asString()));
        // Import document-level custom data
        var docDataExt = ocifDocument.canvasExtensions().stream().filter(e -> e instanceof DataExtension).map(e -> (DataExtension) e).map(e -> e.map().get("cj:docData")).filter(v -> v != null && v.isObject()).findFirst().orElse(null);
        if (docDataExt != null) {
            var o = docDataExt.asObject();
            cjDocument.dataMutable(d -> //
                    o.keys().forEach(k -> {
                        var v = o.get(k);
                        if (v != null) d.add(k, v);
                    }));
        }

        // Fallback: single graph if content exists
        if (!ocifDocument.nodes().isEmpty() || !ocifDocument.relations().isEmpty()) {
            cjDocument.addGraph(cjGraph -> {
                ocifDocument2cjGraph(ocifDocument, cjGraph);
            });
        }
        return cjDocument;
    }

}
