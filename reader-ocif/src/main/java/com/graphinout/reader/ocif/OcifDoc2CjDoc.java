package com.graphinout.reader.ocif;

import com.graphinout.base.cj.document.CjEdgeTypeSource;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.EdgeRelationExtension;

import java.util.Objects;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public class OcifDoc2CjDoc {

    public static ICjDocument toCjDocument(OcifDocument ocifDocument) {
        ICjDocumentMutable cjDocument = new CjDocumentElement();
        cjDocument.addGraph(cjGraph -> {
            for (IOcifNode ocifNode : ocifDocument.nodes()) {
                ICjNode n = cjGraph.addNode(cjNode -> {
                    cjNode.id(ocifNode.id());
                });
                // TODO ext
            }

            for (IOcifRelation ocifRelation : ocifDocument.relations()) {
                for (IOcifExtension ext : ocifRelation.extensions()) {
                    switch (ext) {
                        case EdgeRelationExtension edge -> {
                            assert Objects.equals(edge.typeName(), OCIF.Type.OCIF_REL_EDGE);
                            String startId = edge.start();
                            String endId = edge.end();
                            cjGraph.addEdge(e -> {
                                if (edge.directed()) {
                                    e.addEndpointIncoming(startId);
                                    e.addEndpointOutgoing(endId);
                                } else {
                                    e.addEndpointUndirected(startId);
                                    e.addEndpointUndirected(endId);
                                }
                                ifPresentAccept(edge.rel(), rel -> {
                                    // TODO rel might be a TYPE_URI
                                    e.edgeType(ICjEdgeType.of(CjEdgeTypeSource.String, rel));
                                });
                                ifPresentAccept(edge.node(), nodeId -> {
                                    e.dataMutable(data -> data.addProperty(OCIF.Common.NODE, nodeId));
                                });
                            });
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + ext);
                    }
                }
            }
        });
        return cjDocument;
    }

}
