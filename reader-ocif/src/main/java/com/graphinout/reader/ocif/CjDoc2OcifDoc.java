package com.graphinout.reader.ocif;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.stream.PowerStreams;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.impl.OcifNode;
import com.graphinout.reader.ocif.document.impl.OcifRelation;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public class CjDoc2OcifDoc {

    private static void addCjGraphToOcifDocument(ICjGraph cjGraph, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        cjGraph.nodes().forEach(cjNode -> ocifDocument.addNode(toOcifNode(cjNode, ocifDocument, errorHandler)));
        cjGraph.edges().forEach(cjEdge -> ocifDocument.addRelation(toOcifRelation(cjEdge, ocifDocument, errorHandler)));
    }

    private static IOcifRelationMutable toOcifRelation(ICjEdge cjEdge, OcifDocument ocifDocument, Consumer<ContentError> errorHandler) {
        IOcifRelationMutable ocifRelation = new OcifRelation();
        ocifRelation.setId(ocifDocument.createId());
        return ocifRelation;
    }

    public static OcifDocument toOcifDocument(ICjDocument cjDoc, Consumer<ContentError> errorHandler) {
        OcifDocument ocifDocument = new OcifDocument();

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
                ICjLabelEntry cjEntry = PowerStreams.findOne(cjLabel.entries());
                IOcifResource res = IOcifResource.ofContentText("res-" + ocifDocument.createId(), cjEntry.value());
                // extended data
                res.set(CjConstants.LANGUAGE, cjEntry.language());
                ocifDocument.addResource(res);
            }, "CJ label must have exactly 1 entry for OCIF. Ignored.", errorHandler);
        });
        return ocifNode;
    }

}
