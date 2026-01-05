package com.graphinout.reader.ocif.cj;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMetaMutable;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.reader.ocif.Json2OcifDoc;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.cj.OcifCj.OcifInCj;
import com.graphinout.reader.ocif.document.IOcifDocumentMutable;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifRepresentationMutable;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.IOcifResourceMutable;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.canvas.CjDocumentCanvasExtension;
import com.graphinout.reader.ocif.document.extension.canvas.IOcifCanvasExtension;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.PortsNodeExtension;
import com.graphinout.reader.ocif.document.extension.relation.CjLabelRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.HyperedgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.IOcifRelationExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.impl.OcifNode;
import com.graphinout.reader.ocif.document.impl.OcifRelation;
import com.graphinout.reader.ocif.document.impl.OcifResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public class CjDoc2OcifDoc {

    private static void addCjGraphToOcifDocument(ICjGraph cjGraph, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        cjGraph.nodes().forEach(cjNode -> {
            ocifDocument.addNode(toOcifNode(cjNode, ocifDocument, errorHandler));
            // also export graphs nested inside nodes
            cjNode.graphs().forEach(child -> addCjGraphToOcifDocument(child, ocifDocument, errorHandler));
        });
        cjGraph.edges().forEach(cjEdge -> ocifDocument.addRelation(toOcifRelation(cjEdge, ocifDocument, errorHandler)));
        // recurse into nested graphs
        cjGraph.graphs().forEach(child -> addCjGraphToOcifDocument(child, ocifDocument, errorHandler));
    }

    private static String encodeEndpoint(ICjEndpoint ep) {
        String node = ep.node();
        String port = ep.port();
        if (port == null || port.isEmpty()) return node;
        // encode node#port to preserve port information in OCIF relation endpoint
        return node + "#" + port;
    }

    private static IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

    private static void ocifExtensionsTo(@Nullable ICjData cjData, Consumer<IOcifExtension> consumer, Consumer<ContentError> errorHandler) {
        if (cjData == null) return;
        ifPresentAccept(cjData.getProperty(OcifInCj.OCIF_EXTENSIONS), v -> {
            if (!v.isArray()) {
                throw new IllegalStateException("Expect property '" + OcifInCj.OCIF_EXTENSIONS + "' to be an array but was " + v.jsonType());
            }
            v.asArray().forEach(extVal -> {
                IOcifExtension ocifExtension = Json2OcifDoc.toOcifExtension(extVal.asObject(), errorHandler);
                consumer.accept(ocifExtension);
            });
        });
    }

    private static IJsonObjectMutable portToJsonRecursive(ICjPort p) {
        var obj = factory().createObjectMutable();
        if (p.id() != null) obj.setString(OCIF.Common.ID, p.id());
        // include port data if present
        var data = p.data();
        if (data != null && data.jsonValue() != null) {
            var jv = data.jsonValue();
            if (jv != null && jv.isObject()) {
                obj.setObject(OCIF.Common.DATA, jv.asObject());
            }
        }
        var children = factory().createArrayMutable();
        p.ports().forEach(cp -> children.add(portToJsonRecursive(cp)));
        if (!children.isEmpty()) obj.setArray(OCIF.Common.PORTS, children);
        return obj;
    }

    /** CJ to OCIF */
    public static OcifDocument toOcifDocument(ICjDocument cjDoc, Consumer<ContentError> errorHandler) {
        OcifDocument ocifDocument = new OcifDocument();

        // Restore previously exported OCIF data, if preserved in CJ data
        ifPresentAccept(cjDoc.data(), cjData -> {
            IJsonValue jsonValue = cjData.jsonValue();
            if (jsonValue == null) {
                // don't add to OCIF
                return;
            }
            DataExtension unknownCjDataProperties = new DataExtension();
            if (jsonValue.isObject()) {
                // inspect properties
                jsonValue.asObject().forEach((k, v) -> {
                    switch (k) {
                        case OcifInCj.OCIF_DOCUMENT -> OcifDocData.toOcifDocument(v, ocifDocument);
                        case OcifInCj.OCIF_EXTENSIONS -> // Restore preserved OCIF extensions
                                ocifExtensionsTo(cjData, ext -> ocifDocument.addCanvasExtension((IOcifCanvasExtension) ext), errorHandler);
                        default -> {
                            // Export CJ->OCIF: document-level custom data as canvas-level DataExtension
                            if (!k.equals(OCIF.Common.TYPE) || !v.isString() || !v.asString().equals(DataExtension.TYPE)) {
                                unknownCjDataProperties.set(k, v);
                            }
                            // else: skip
                        }
                    }
                });
            } else {
                unknownCjDataProperties.set(OcifCj.CjInOcifData.DATA_NON_OBJECT, jsonValue);
            }
            if (!unknownCjDataProperties.isEmpty()) {
                ocifDocument.addCanvasExtension(unknownCjDataProperties);
            }
        });

        // Export CJ->OCIF: document-level baseUri as canvas extension
        CjDocumentCanvasExtension cjDocumentCanvasExtension = new CjDocumentCanvasExtension();
        ifPresentAccept(cjDoc.baseUri(), cjDocumentCanvasExtension::baseUri);
        ifPresentAccept(cjDoc.connectedJson(), ICjElement::toJsonValue,
                ICjDocumentMetaMutable::of,
                cjDocumentCanvasExtension::connectedJson);
        if (!cjDocumentCanvasExtension.isEmpty()) {
            ocifDocument.addCanvasExtension(cjDocumentCanvasExtension);
        }

        try {
            ICjGraph cjGraph = cjDoc.theGraph();
            if (cjGraph != null) {
                addCjGraphToOcifDocument(cjGraph, ocifDocument, errorHandler);
            }
        } catch (IllegalStateException e) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, "Can only export 1 graph tp OCIF", Location.UNAVAILABLE));
        }

        return ocifDocument;
    }

    private static HyperedgeRelationExtension toOcifHyperEdgeRelationExtension(ICjEdge cjEdge) {
        var cjEndpointsList = cjEdge.endpoints().toList();
        var hyper = new HyperedgeRelationExtension();
        var arr = factory().createArrayMutable();
        for (var ep : cjEndpointsList) {
            String id = encodeEndpoint(ep);
            String dir = HyperedgeRelationExtension.Endpoint.ocifDirection(ep.direction());
            var epObj = factory().createObjectMutable();
            epObj.setString(OCIF.Common.ID, id);
            epObj.setString(OCIF.Common.DIRECTION, dir);
            arr.add(epObj);
            // also set typed field
            hyper.addEndpoint(new HyperedgeRelationExtension.Endpoint().id(id).direction(dir));
        }
        ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, hyper::setRel);
        return hyper;
    }

    private static @NonNull IOcifNodeMutable toOcifNode(ICjNode cjNode, IOcifDocumentMutable ocifDocument, Consumer<ContentError> errorHandler) {
        IOcifNodeMutable ocifNode = new OcifNode();

        ocifNode.id(cjNode.id());

        // Restore previously exported OCIF data, if preserved in CJ data
        ifPresentAccept(cjNode.data(), cjData -> {
            IJsonValue jsonValue = cjData.jsonValue();
            if (jsonValue == null) {
                // don't add to OCIF
                return;
            }
            // to hold native CJ data properties not recognized by OCIF
            DataExtension unknownCjDataProperties = new DataExtension();

            if (jsonValue.isObject()) {
                // inspect properties
                jsonValue.asObject().forEach((k, v) -> {
                    switch (k) {
                        case OcifInCj.OCIF_NODE ->  // map known OCIF node props
                                OcifNodeData.toOcifNode(v, ocifNode);
                        case OcifInCj.OCIF_EXTENSIONS -> // Restore preserved OCIF extensions
                                ocifExtensionsTo(cjData, ext -> ocifNode.addNodeExtension((IOcifNodeExtension) ext), errorHandler);
                        default -> {
                            // Export CJ->OCIF: node-level custom data as node-level DataExtension
                            if (!k.equals(OCIF.Common.TYPE) || !v.isString() || !v.asString().equals(DataExtension.TYPE)) {
                                unknownCjDataProperties.set(k, v);
                            }
                            // else: skip
                        }
                    }
                });
            } else {
                unknownCjDataProperties.set(OcifCj.CjInOcifData.DATA_NON_OBJECT, jsonValue);
            }
            if (!unknownCjDataProperties.isEmpty()) {
                ocifNode.addNodeExtension(unknownCjDataProperties);
            }
        });

        // FIXME HERE ==============================================================
        ifPresentAccept(cjNode.label(), cjLabel -> {
            ContentError.try_(() -> {
                // Does the node.resource exist in the resources array?
                // Otherwise the label is representing a resource.
                String resourceId = ocifNode.resource();
                boolean create = false;
                if (resourceId == null) {
                    // OCIF node has no resource yet, but we have a CJ label => set as resource
                    resourceId = "res-" + cjNode.id();
                    ocifNode.resource(resourceId);
                    create = true;
                } else {
                    IOcifResource ocifResource = ocifDocument.resourceById(resourceId);
                    if (ocifResource == null) {
                        // we need to represent the label as resource
                        create = true;
                    }
                }
                if (create) {
                    // we need to represent the label as resource
                    IOcifResource labelAsResource = toOcifResource(resourceId, cjLabel);
                    ocifDocument.resources().add(labelAsResource);
                }
            }, "Failed to export CJ labels.", errorHandler);
        });

        // Export ports recursively as DataExtension (cj:ports)
        if (cjNode.hasPorts()) {
            PortsNodeExtension portsNodeExtension = toOcifPortExtension(cjNode);
            ocifNode.addNodeExtension(portsNodeExtension);
        }

        return ocifNode;
    }

    private static PortsNodeExtension toOcifPortExtension(ICjNode cjNode) {
        assert cjNode.hasPorts();
        PortsNodeExtension portsNodeExtension = new PortsNodeExtension();

        var portsArray = factory().createArrayMutable();
        cjNode.ports().forEach(p -> portsArray.add(portToJsonRecursive(p)));
        assert portsArray.size() == cjNode.ports().count();

        portsNodeExtension.ports(portsArray.asListOfStrings());
        return portsNodeExtension;
    }

    private static IOcifRelationMutable toOcifRelation(ICjEdge cjEdge, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        IOcifRelationMutable ocifRelation = new OcifRelation();
        // Preserve edge ID if present
        if (cjEdge.id() != null) {
            ocifRelation.id(cjEdge.id());
        }

        // Map CJ edge to OCIF EdgeRelationExtension
        // ... Determine start/end and direction ...

        // Restore preserved OCIF extensions
        ifPresentAccept(cjEdge.data(), ICjData::jsonValue, jsonValue -> {
            if (jsonValue.isObject()) {
                IJsonObject o = jsonValue.asObject();
                DataExtension unknownProperties = new DataExtension();
                o.forEach((k, v) -> {
                    switch (k) {
                        case OcifInCj.OCIF_EXTENSIONS -> {
                            if (v.isArray()) {
                                v.asArray().forEach(extVal -> {
                                    ocifRelation.addExtension((IOcifRelationExtension) Json2OcifDoc.toOcifExtension(extVal.asObject(), errorHandler));
                                });
                            } else {
                                throw new IllegalStateException("Found non-array value for OCIF_EXTENSIONS");
                            }
                        }
                        case OcifInCj.OCIF_RELATION -> {
                            OcifRelationData ocifRelationData = new OcifRelationData(v.asObject().mutableCopy());
                            ifPresentAccept(ocifRelationData.node(), ocifRelation::node);
                        }
                        default -> // Export CJ->OCIF: edge-level custom data as edge-level DataExtension
                                unknownProperties.set(k, v);
                    }
                });
                if (!unknownProperties.isEmpty()) {
                    ocifRelation.addExtension(unknownProperties);
                }
            } else {
                // copy to OCIF data extension
                DataExtension data = new DataExtension();
                data.set(OcifCj.CjInOcifData.DATA_NON_OBJECT, jsonValue);
                ocifRelation.addExtension(data);
            }
        });

        ICjEndpoint source = null;
        ICjEndpoint target = null;
        boolean directed = true;
        try {
            source = cjEdge.source();
            target = cjEdge.target();
            if (source == null || target == null) {
                // if no explicit IN/OUT, treat as undirected using the first two endpoints when available
                var eps = cjEdge.endpoints().toList();
                if (eps.size() >= 2) {
                    source = eps.get(0);
                    target = eps.get(1);
                    // mark undirected if both endpoints are undirected
                    directed = eps.stream().anyMatch(ICjEndpoint::isDirected);
                }
            }
        } catch (IllegalStateException ex) {
            // Multiple sources/targets (hyperedge). Fallback to first two endpoints, directed if any directed.
            var eps = cjEdge.endpoints().toList();
            if (eps.size() >= 2) {
                source = eps.get(0);
                target = eps.get(1);
                directed = eps.stream().anyMatch(ICjEndpoint::isDirected);
            }
        }


        if (cjEdge.endpoints().count() > 2) {
            // Hyperedge
            HyperedgeRelationExtension hyper = toOcifHyperEdgeRelationExtension(cjEdge);

            ocifRelation.addExtension(hyper);
        } else if (source != null && target != null) {
            String start = encodeEndpoint(source);
            String end = encodeEndpoint(target);

            IJsonObjectMutable extObj = factory().createObjectMutable();
            extObj.setString(OCIF.Common.TYPE, EdgeRelationExtension.TYPE_URI);
            extObj.setString(OCIF.Common.START, start);
            extObj.setString(OCIF.Common.END, end);
            extObj.setBoolean(OCIF.Common.DIRECTED, directed);

            ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, type -> //
                    extObj.setString(OCIF.Common.REL, type));
            // create typed extension instance and populate map so it serializes
            EdgeRelationExtension edgeExt = EdgeRelationExtension.of(extObj);
            edgeExt.start(start);
            edgeExt.end(end);
            edgeExt.directed(directed);


            ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, edgeExt::rel);
            ocifRelation.addExtension(edgeExt);
        }

        // Export edge labels as CjLabelRelationExtension
        ifPresentAccept(cjEdge.label(), cjLabel -> {
            CjLabelRelationExtension cjLabelRelationExtension = CjLabelRelationExtension.of(cjLabel);
            ocifRelation.addExtension(cjLabelRelationExtension);
        });

        return ocifRelation;
    }

    private static IOcifResource toOcifResource(String resourceId, @NonNull ICjLabel cjLabel) {
        IOcifResourceMutable ocifResource = new OcifResource(resourceId);
        cjLabel.entries().forEach(cjLabelEntry -> {
            String content = cjLabelEntry.value();
            IOcifRepresentationMutable ocifRep = IOcifRepresentation.ofContent(content, "text/plain");
            ifPresentAccept(cjLabelEntry.language(), language -> {
                DataExtension ocifData = new DataExtension();
                ocifData.set(CjConstants.LANGUAGE, language);
                ocifRep.addExtension(ocifData);
            });
            ocifResource.addRepresentation(ocifRep);
        });
        return ocifResource;
    }

}
