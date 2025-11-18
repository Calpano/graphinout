package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjGraphMutable extends ICjGraph, ICjHasGraphsMutable, ICjGraphChunkMutable {

    /**
     * @param sourceNodeId incoming
     * @param targetNodeId outgoing
     */
    default void addBiEdge(String sourceNodeId, String targetNodeId) {
        addEdge(edge -> {
            edge.addEndpointIncoming(sourceNodeId);
            edge.addEndpointOutgoing(targetNodeId);
        });
    }

    void addEdge(Consumer<ICjEdgeMutable> edge);

    void addNode(Consumer<ICjNodeMutable> node);

}
