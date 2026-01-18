package com.graphinout.reader.ocif.cj;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.cj.OcifCj.OcifInCj;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.canvas.CjDocumentCanvasExtension;
import com.graphinout.reader.ocif.document.extension.canvas.IOcifCanvasExtension;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.PortsNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.TextStyleNodeExtension;
import com.graphinout.reader.ocif.document.extension.relation.CjLabelRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.HyperedgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.representation.CjLanguageRepresentationExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.stream.PowerStreams.filterMap;
import static org.slf4j.LoggerFactory.getLogger;

public class OcifDoc2CjDoc {


    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String PORTS = "ports";
    public static final String _OCIF_ = "ocif";
    public static final String LABEL = "label";
    private static final Logger log = getLogger(OcifDoc2CjDoc.class);

    private static IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

    private static void importPortsRecursive(ICjNodeMutable node, com.graphinout.foundation.pure.json.document.IJsonArray ports) {
        for (int i = 0; i < ports.size(); i++) {
            var pobj = ports.get_(i).asObject();
            String id = pobj.getString(ID);
            node.addPort(pm -> {
                if (id != null) pm.id(id);
                var data = pobj.get(DATA);
                if (data != null && data.isObject()) {
                    var o = data.asObject();
                    pm.dataMutable(d -> //
                            o.keys().forEach(k -> {
                                var v = o.get(k);
                                if (v != null) d.add(k, v);
                            }));
                }
                var children = pobj.get(PORTS);
                if (children != null && children.isArray()) {
                    importSubPorts(pm, children.asArray());
                }
            });
        }
    }

    private static void importSubPorts(com.graphinout.base.cj.document.ICjPortMutable parent, com.graphinout.foundation.pure.json.document.IJsonArray ports) {
        for (int i = 0; i < ports.size(); i++) {
            var pobj = ports.get_(i).asObject();
            String id = pobj.getString(ID);
            parent.addPort(pm -> {
                if (id != null) pm.id(id);
                var data = pobj.get(DATA);
                if (data != null && data.isObject()) {
                    var o = data.asObject();
                    pm.dataMutable(d -> //
                            o.keys().forEach(k -> {
                                var v = o.get(k);
                                if (v != null) d.add(k, v);
                            }));
                }
                var children = pobj.get(PORTS);
                if (children != null && children.isArray()) {
                    importSubPorts(pm, children.asArray());
                }
            });
        }
    }

    private static void ocifDocument2cjDocument(OcifDocument ocifDocument, ICjDocumentMutable cjDocument) {
        // restore CJ document level properties from canvas extension
        IJsonArrayMutable unknownExt = factory().createArrayMutable();
        for (IOcifCanvasExtension ext : ocifDocument.canvasExtensions()) {
            if (ext instanceof CjDocumentCanvasExtension cjDocumentCanvasExt) {
                ifPresentAccept(cjDocumentCanvasExt.baseUri(), cjDocument::baseUri);
                ifPresentAccept(cjDocumentCanvasExt.connectedJson(), cjDocument::connectedJson);
            } else if (ext instanceof DataExtension dataExt) {
                // Import OCIF document-level custom data as CJ data
                cjDocument.dataMutable(d -> d.setJsonValue(dataExt.toJson()));
            } else {
                unknownExt.add(ext.toJson());
            }
        }
        if (!unknownExt.isEmpty()) {
            cjDocument.dataMutable(d -> d.add(OcifInCj.OCIF_EXTENSIONS, unknownExt));
        }


        // OCIF canvas to CJ graph
        if (!ocifDocument.nodes().isEmpty() || !ocifDocument.relations().isEmpty()) {
            cjDocument.addGraph(cjGraph -> ocifDocument2cjGraph(ocifDocument, cjGraph));
        }

        // preserve OCIF document level data (ocif, rootNode, resources, schemas) as CJ data
        OcifDocData ocifDocLevelData = OcifDocData.ofOcifDoc(ocifDocument);

        if (!ocifDocLevelData.isEmpty()) {
            cjDocument.dataMutable(d -> d.add(OcifInCj.OCIF_DOCUMENT, ocifDocLevelData.jsonObject()));
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
        // direct native OCIF properties to native CJ properties
        cjNode.id(ocifNode.id());

        // restore CJ document level properties from canvas extension
        IJsonArrayMutable unknownExts = factory().createArrayMutable();
        for (IOcifNodeExtension ext : ocifNode.extensions()) {
            switch (ext) {
                case PortsNodeExtension portsNodeExtension ->
                    // TODO handle known node extensions
                    // ifPresentAccept(portsNodeExtension.ports(), cjNode::ports);
                    // importPortsRecursive(cjNode, v.asArray());
                        unknownExts.add(ext.toJson());
                case TextStyleNodeExtension textStyleNodeExtension ->
                    // TODO handle known node extensions
                    // textStyleNodeExtension.color();
                        unknownExts.add(ext.toJson());
                case DataExtension dataExt ->
                    // Import OCIF node-level custom data as CJ data
                        cjNode.dataMutable(d ->
                                d.setJsonValue(dataExt.toJson()));
                case null -> {
                    log.warn("null in extensions data array");
                }
                default -> unknownExts.add(ext.toJson());
            }
        }
        if (!unknownExts.isEmpty()) {
            cjNode.dataMutable(d -> d.add(OcifInCj.OCIF_EXTENSIONS, unknownExts));
        }

        // preserve OCIF node-level data (ocif, rootNode, resources, schemas) as CJ data
        OcifNodeData ocifNodeData = OcifNodeData.ofOcifNode(ocifNode);
        String resourceId = ocifNodeData.resource();

        if (resourceId != null) {
            IOcifResource resource = ocifDocument.findResource(resourceId).orElse(null);
            if (resource == null) {
                throw new IllegalStateException("OCIF node '" + ocifNode.id() + "' references resource '" + resourceId + "', which was not found.");
            }
            boolean isResourceFullyRepresentedALabel = ocifResource2cjLabel(resource, cjNode);
            if (isResourceFullyRepresentedALabel) {
                ocifDocument.removeResourceById(resourceId);
            }
        }

        if (!ocifNodeData.isEmpty()) {
            cjNode.dataMutable(d -> d.add(OcifInCj.OCIF_NODE, ocifNodeData.jsonObject()));
        }
    }

    private static void ocifRelation2cjEdge(IOcifRelation ocifRelation, ICjEdgeMutable cjEdge) {
        // Preserve relation/edge ID only if not auto-generated (a*)
        if (ocifRelation.id() != null && !ocifRelation.id().matches("a\\d+")) {
            cjEdge.id(ocifRelation.id());
        }

        OcifRelationData ocifRelationData = new OcifRelationData(factory().createObjectMutable());
        ifPresentAccept(ocifRelation.node(), ocifRelationData::node);
        if (!ocifRelationData.isEmpty()) {
            cjEdge.dataMutable(d -> d.add(OcifInCj.OCIF_RELATION, ocifRelationData.jsonObject()));
        }

        var unknownExtensions = IJsonFactory.INSTANCE.createArrayMutable();
        for (IOcifExtension ext : ocifRelation.extensions()) {
            switch (ext) {
                case DataExtension ocifData -> cjEdge.dataMutable(cjData ->
                        cjData.setJsonValue(ocifData.toJson()));
                case CjLabelRelationExtension cjLabelRelation -> //
                        cjLabelRelation.label().entries().forEach(cjEntry -> //
                                cjEdge.addLabel(cjEntry.value(), cjEntry.language()));
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
                        cjEdge.edgeType(ICjEdgeType.of(rel));
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
                        cjEdge.edgeType(ICjEdgeType.of(hex.rel()));
                    }
                }
                default -> unknownExtensions.add(ext.toJson());
            }
        }
        if (!unknownExtensions.isEmpty()) {
            cjEdge.dataMutable(d -> d.add("cj:ocifExtensions", unknownExtensions));
        }
    }

    private static <T extends ICjHasLabelMutable & ICjHasDataMutable> boolean ocifResource2cjLabel(IOcifResource resource, @NonNull ICjNodeMutable cjEntity) {
        List<IOcifRepresentation> repList = resource.representations();
        if (repList == null || repList.isEmpty()) {
            throw new IllegalStateException("OCIF resource '" + resource.id() + "' has no representations.");
        }

        // Check if there are any TEXT_PLAIN representations before creating a label
        boolean hasTextPlainReps = repList.stream().anyMatch(rep -> rep.matchesMimeType(IOcifResource.TEXT_PLAIN));

        if (hasTextPlainReps) {
            // transform each text/plain resource representation into a CJ label entry, possibly respecting a CjLanguageExtension
            cjEntity.labelMutable(cjLabel -> {
                repList.stream().filter(rep -> rep.matchesMimeType(IOcifResource.TEXT_PLAIN)).forEach(rep -> {
                    assert rep.content() != null;
                    assert rep.mimeType() != null;
                    assert rep.mimeType().equals(IOcifResource.TEXT_PLAIN);
                    cjLabel.addEntry(labelEntry -> {
                        labelEntry.value(rep.content());
                        // language of representation is in OCIF-CJ Label Node Extension of that representation
                        filterMap(rep.extensions().stream(), CjLanguageRepresentationExtension.class).findFirst() //
                                .ifPresent(langExt -> labelEntry.language(langExt.language()));
                    });
                });
            });
        }

        // if we can fully represent the resource as a CJ label, remove it from doc/resources
        return resource.isAllRepresentationsAreTextPlain();
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
        ocifDocument2cjDocument(ocifDocument, cjDocument);
        return cjDocument;
    }

}
