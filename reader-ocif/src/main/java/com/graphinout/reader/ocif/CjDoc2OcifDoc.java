package com.graphinout.reader.ocif;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.extension.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.impl.OcifNode;
import com.graphinout.reader.ocif.document.impl.OcifRelation;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

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


    private static IJsonObjectMutable portToJsonRecursive(ICjPort p) {
        var obj = IJsonFactory.INSTANCE.createObjectMutable();
        if (p.id() != null) obj.setString("id", p.id());
        // include port data if present
        var data = p.data();
        if (data != null && data.jsonValue() != null) {
            var jv = data.jsonValue();
            if (jv != null && jv.isObject()) {
                obj.setObject("data", jv.asObject());
            }
        }
        var children = IJsonFactory.INSTANCE.createArrayMutable();
        p.ports().forEach(cp -> children.add(portToJsonRecursive(cp)));
        if (!children.isEmpty()) obj.setArray("ports", children);
        return obj;
    }

    public static OcifDocument toOcifDocument(ICjDocument cjDoc, Consumer<ContentError> errorHandler) {
        OcifDocument ocifDocument = new OcifDocument();

        // Restore OCIF schema URI and resources if preserved in CJ data
        ifPresentAccept(cjDoc.data(), data -> {
            ifPresentAccept(data.getProperty("cj:ocifSchema"), v -> ocifDocument.setOcifSchemaURI(v.asString()));
            ifPresentAccept(data.getProperty("cj:ocifResources"), v -> {
                if (v.isArray()) {
                    v.asArray().forEach(rv -> {
                        try {
                            ocifDocument.addResource(Json2OcifDoc.toOcifResource(rv.asObject(), ocifDocument::createId, errorHandler));
                        } catch (Exception e) {
                            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, "Failed to restore OCIF resource: " + e.getMessage(), Location.UNAVAILABLE));
                        }
                    });
                }
            });
        });

        // Export document-level baseUri as canvas extension
        ifPresentAccept(cjDoc.baseUri(), base -> {
            var extObj = IJsonFactory.INSTANCE.createObjectMutable();
            extObj.setString("cj:baseUri", base);
            ocifDocument.addCanvasExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
        });

        // Export document-level custom data as canvas extension
        ifPresentAccept(cjDoc.data(), data -> {
            var json = data.jsonValue();
            if (json != null && json.isObject()) {
                var remaining = IJsonFactory.INSTANCE.createObjectMutable();
                var obj = json.asObject();
                obj.keys().forEach(k -> {
                    if (!k.startsWith("cj:")) {
                        var v = obj.get(k);
                        if (v != null) remaining.setProperty(k, v);
                    }
                });
                if (!remaining.isEmpty()) {
                    var extObj = IJsonFactory.INSTANCE.createObjectMutable();
                    extObj.setObject("cj:docData", remaining);
                    ocifDocument.addCanvasExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
                }
            }
        });

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

    private static @NonNull IOcifNodeMutable toOcifNode(ICjNode cjNode, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        IOcifNodeMutable ocifNode = new OcifNode();
        ocifNode.setId(cjNode.id());
        ifPresentAccept(cjNode.label(), cjLabel -> {
            ContentError.try_(() -> {
                // Export all label entries into resources and one DataExtension array
                var entries = cjLabel.entries().toList();
                if (!entries.isEmpty()) {
                    var labelsArray = IJsonFactory.INSTANCE.createArrayMutable();
                    for (int i = 0; i < entries.size(); i++) {
                        ICjLabelEntry e = entries.get(i);
                        IOcifResource res = IOcifResource.ofContentText("res-" + ocifDocument.createId(), e.value());
                        res.set(CjConstants.LANGUAGE, e.language());
                        ocifDocument.addResource(res);
                        // remember first as primary node resource (for viewers)
                        if (i == 0) ocifNode.setResource(res.id());

                        var lobj = IJsonFactory.INSTANCE.createObjectMutable();
                        lobj.setString("resource", res.id());
                        lobj.setString("value", e.value());
                        if (e.language() != null) lobj.setString(CjConstants.LANGUAGE, e.language());
                        labelsArray.add(lobj);
                    }
                    // attach DataExtension with key "cj:labels"
                    var extObj = IJsonFactory.INSTANCE.createObjectMutable();
                    extObj.setArray("cj:labels", labelsArray);
                    ocifNode.addExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
                }
            }, "Failed to export CJ labels.", errorHandler);
        });
        ifPresentAccept(cjNode.data(), cjData -> {
            // Restore preserved OCIF extensions
            ifPresentAccept(cjData.getProperty("cj:ocifExtensions"), v -> {
                if (v.isArray()) {
                    v.asArray().forEach(extVal -> {
                        ocifNode.addExtension(Json2OcifDoc.toOcifExtension(extVal.asObject(), errorHandler));
                    });
                }
            });

            // map known OCIF node props
            ifPresentAccept(cjData.getProperty(OCIF.Node.POSITION), json -> ocifNode.setPosition(OcifVector23D.of(json)));
            ifPresentAccept(cjData.getProperty(OCIF.Node.SIZE), json -> ocifNode.setSize(OcifVector23D.of(json)));
            ifPresentAccept(cjData.getProperty(OCIF.Node.RESOURCE_FIT), s -> ocifNode.setResourceFit(IOcifNodeMutable.ResourceFit.valueOf(s.asString())));
            ifPresentAccept(cjData.getProperty(OCIF.Node.ROTATION), n -> ocifNode.setRotation(n.asNumber().doubleValue()));

            // map optional resource reference from CJ data if present
            ifPresentAccept(cjData.getProperty(OCIF.Node.RESOURCE), v -> ocifNode.setResource(v.asString()));

            // map optional back-reference to relation if present
            ifPresentAccept(cjData.getProperty(OCIF.Node.RELATION), v -> ocifNode.setRelation(v.asString()));

            // export remaining CJ node data to DataExtension (cj:data)
            var remaining = IJsonFactory.INSTANCE.createObjectMutable();
            var dataVal = cjData.jsonValue();
            if (dataVal != null && dataVal.isObject()) {
                var obj = dataVal.asObject();
                obj.keys().forEach(k -> {
                    if (!OCIF.Node.POSITION.equals(k) && !OCIF.Node.SIZE.equals(k) && !OCIF.Node.RESOURCE_FIT.equals(k) && !OCIF.Node.ROTATION.equals(k) && !OCIF.Node.RESOURCE.equals(k) && !OCIF.Node.RELATION.equals(k) && !k.startsWith("cj:")) {
                        var v = obj.get(k);
                        if (v != null) remaining.setProperty(k, v);
                    }
                });
            }
            if (!remaining.isEmpty()) {
                var extObj = IJsonFactory.INSTANCE.createObjectMutable();
                extObj.setObject("cj:data", remaining);
                ocifNode.addExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
            }
        });
        // Export ports recursively as DataExtension (cj:ports)
        var portsArray = IJsonFactory.INSTANCE.createArrayMutable();
        cjNode.ports().forEach(p -> portsArray.add(portToJsonRecursive(p)));
        if (!portsArray.isEmpty()) {
            var extObj = IJsonFactory.INSTANCE.createObjectMutable();
            extObj.setArray("cj:ports", portsArray);
            ocifNode.addExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
        }
        return ocifNode;
    }

    private static IOcifRelationMutable toOcifRelation(ICjEdge cjEdge, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        IOcifRelationMutable ocifRelation = new OcifRelation();
        // Preserve edge ID if present
        if (cjEdge.id() != null) {
            ocifRelation.setId(cjEdge.id());
        }

        // Map CJ edge to OCIF EdgeRelationExtension
        // ... Determine start/end and direction ...

        // Restore preserved OCIF extensions
        ifPresentAccept(cjEdge.data(), data -> {
            ifPresentAccept(data.getProperty("cj:ocifExtensions"), v -> {
                if (v.isArray()) {
                    v.asArray().forEach(extVal -> {
                        ocifRelation.addExtension(Json2OcifDoc.toOcifExtension(extVal.asObject(), errorHandler));
                    });
                }
            });
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

        var endpointsList = cjEdge.endpoints().toList();
        if (endpointsList.size() > 2) {
            // Hyperedge
            var hyper = new com.graphinout.reader.ocif.document.extension.HyperedgeRelationExtension();
            var arr = IJsonFactory.INSTANCE.createArrayMutable();
            for (var ep : endpointsList) {
                String id = encodeEndpoint(ep);
                String dir = ep.direction() == CjDirection.OUT ? "out" : (ep.direction() == CjDirection.IN ? "in" : "undir");
                var epObj = IJsonFactory.INSTANCE.createObjectMutable();
                epObj.setString(OCIF.Common.ID, id);
                epObj.setString(OCIF.Common.DIRECTION, dir);
                arr.add(epObj);
                // also set typed field
                hyper.addEndpoint(new com.graphinout.reader.ocif.document.extension.HyperedgeRelationExtension.Endpoint().setId(id).setDirection(dir));
            }
            ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, type -> {
                hyper.setRel(type);
                hyper.set(OCIF.Common.REL, type);
            });
            hyper.set(OCIF.Common.ENDPOINTS, arr);
            ocifRelation.addExtension(hyper);
        } else if (source != null && target != null) {
            String start = encodeEndpoint(source);
            String end = encodeEndpoint(target);

            IJsonObjectMutable extObj = IJsonFactory.INSTANCE.createObjectMutable();
            extObj.setString("type", EdgeRelationExtension.TYPE_URI);
            extObj.setString(OCIF.Common.START, start);
            extObj.setString(OCIF.Common.END, end);
            extObj.setBoolean(OCIF.Common.DIRECTED, directed);

            ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, type -> {
                extObj.setString(OCIF.Common.REL, type);
            });
            // create typed extension instance and populate map so it serializes
            EdgeRelationExtension edgeExt = EdgeRelationExtension.of(extObj);
            edgeExt.set(OCIF.Common.START, start);
            edgeExt.set(OCIF.Common.END, end);
            edgeExt.set(OCIF.Common.DIRECTED, directed);

            ifPresentAccept(cjEdge.edgeType(), ICjEdgeType::type, type -> {
                edgeExt.set(OCIF.Common.REL, type);
                extObj.setString(OCIF.Common.REL, type);
            });
            ocifRelation.addExtension(edgeExt);
        }

        // Export edge labels as DataExtension (cj:labels)
        ifPresentAccept(cjEdge.label(), label -> {
            var entries = label.entries().toList();
            if (!entries.isEmpty()) {
                var labelsArray = IJsonFactory.INSTANCE.createArrayMutable();
                for (var le : entries) {
                    var lobj = IJsonFactory.INSTANCE.createObjectMutable();
                    lobj.setString("value", le.value());
                    if (le.language() != null) lobj.setString(CjConstants.LANGUAGE, le.language());
                    labelsArray.add(lobj);
                }
                var extObj = IJsonFactory.INSTANCE.createObjectMutable();
                extObj.setArray("cj:labels", labelsArray);
                ocifRelation.addExtension(com.graphinout.reader.ocif.document.extension.DataExtension.of(extObj));
            }
        });

        return ocifRelation;
    }

}
