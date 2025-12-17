package com.graphinout.reader.ocif;

import com.graphinout.base.cj.document.CjEdgeTypeSource;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;

import java.util.Objects;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;
import static com.graphinout.reader.ocif.OcifDoc2Json.extensionToJson;

public class OcifDoc2CjDoc {

    private static void ocifDocument2cjGraph(OcifDocument ocifDocument, ICjGraphMutable cjGraph) {
        for (IOcifNode ocifNode : ocifDocument.nodes()) {
            cjGraph.addNode(cjNode -> ocifNode2cjNode(ocifNode, cjNode));
        }
        for (IOcifRelation ocifRelation : ocifDocument.relations()) {
            cjGraph.addEdge(cjEdge -> ocifRelation2cjEdge(ocifRelation, cjEdge));
        }
    }

    private static void ocifNode2cjNode(IOcifNode ocifNode, ICjNodeMutable cjNode) {
        cjNode.id(ocifNode.id());
        // add all the other node props
        ifPresentAccept(ocifNode.position(), p -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.POSITION), p.toJson())));
        ifPresentAccept(ocifNode.size(), p -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.SIZE), p.toJson())));
        ifPresentAccept(ocifNode.resource(), r -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.RESOURCE), r)));
        ifPresentAccept(ocifNode.resourceFit(), r -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.RESOURCE_FIT), r.name())));
        ifPresentAccept(ocifNode.rotation(), r -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.ROTATION), IJsonFactory.INSTANCE.createNumber(r))));
        ifPresentAccept(ocifNode.relation(), r -> //
                cjNode.dataMutable(d -> d.add(pathOf(OCIF.Node.RELATION), r)));
        ifPresentAccept(ocifNode.extensions(), exts -> {
            cjNode.dataMutable(data -> {
                IJsonArrayMutable arrayMutable = IJsonFactory.INSTANCE.createArrayMutable();
                for (IOcifExtension ext : exts) {
                    arrayMutable.add(extensionToJson(ext));
                }
                data.addProperty(OCIF.Common.DATA, arrayMutable);
            });
        });
    }

    private static void ocifRelation2cjEdge(IOcifRelation ocifRelation, ICjEdgeMutable cjEdge) {
        for (IOcifExtension ext : ocifRelation.extensions()) {
            switch (ext) {
                case DataExtension ocifData -> {
                    cjEdge.dataMutable(cjData -> {
                        ocifData.map().forEach(cjData::addProperty);
                    });
                }
                case EdgeRelationExtension edge -> {
                    assert Objects.equals(edge.typeName(), OCIF.Type.OCIF_REL_EDGE);
                    String startId = edge.start();
                    String endId = edge.end();
                    if (edge.directed()) {
                        cjEdge.addEndpointIncoming(startId);
                        cjEdge.addEndpointOutgoing(endId);
                    } else {
                        cjEdge.addEndpointUndirected(startId);
                        cjEdge.addEndpointUndirected(endId);
                    }
                    ifPresentAccept(edge.rel(), rel -> {
                        // TODO rel might be a TYPE_URI
                        cjEdge.edgeType(ICjEdgeType.of(CjEdgeTypeSource.String, rel));
                    });
                    ifPresentAccept(edge.node(), nodeId -> {
                        cjEdge.dataMutable(data -> data.addProperty(OCIF.Common.NODE, nodeId));
                    });
                }
                default -> throw new IllegalStateException("Cannot represent extension in CJ: " + ext);
            }
        }
    }

    public static ICjDocument toCjDocument(OcifDocument ocifDocument) {
        ICjDocumentMutable cjDocument = new CjDocumentElement();
        cjDocument.addGraph(cjGraph -> {
            ocifDocument2cjGraph(ocifDocument, cjGraph);
        });
        return cjDocument;
    }

}
