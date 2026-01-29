package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.value.ObjectRef;

import java.util.function.Consumer;

public interface ICjGraphMutable extends ICjGraph, ICjHasGraphsMutable, ICjGraphChunkMutable, ICjHasLabelMutable {

    /**
     * Convenience method
     *
     * @param sourceNodeId incoming
     * @param targetNodeId outgoing
     * @return the created edge as a mutable variant for further modification
     */
    default ICjEdgeMutable addBiEdge(String sourceNodeId, String targetNodeId) {
        ObjectRef<ICjEdgeMutable> edgeMutableRef = ObjectRef.createNull();
        addEdge(edge -> {
            edgeMutableRef.set(edge);
            edge.addEndpointIncoming(sourceNodeId);
            edge.addEndpointOutgoing(targetNodeId);
        });
        return edgeMutableRef.get();
    }

    void addEdge(Consumer<ICjEdgeMutable> edge);

    ICjNodeMutable addNode(Consumer<ICjNodeMutable> node);

    default ICjNodeMutable addNode() {
        return addNode(node -> {});
    }

    void removeNode(ICjNode node);

    void removeEdge(ICjEdge edge);

}
