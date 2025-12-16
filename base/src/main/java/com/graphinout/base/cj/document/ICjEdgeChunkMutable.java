package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjEdgeChunkMutable extends ICjChunkMutable, ICjEdgeChunk, ICjHasIdMutable, ICjHasLabelMutable, ICjHasDataMutable {

    /** Consumer should finish with a valid endpoint. */
    void addEndpoint(Consumer<ICjEndpointMutable> endpoint);

    /** Incoming endpoint (from the perspective of the edge) */
    default void addEndpointIncoming(String nodeId) {
        addEndpoint(ep -> ep.direction(CjDirection.IN).node(nodeId));
    }

    /** Outgoing endpoint (from the perspective of the edge) */
    default void addEndpointOutgoing(String nodeId) {
        addEndpoint(ep -> ep.direction(CjDirection.OUT).node(nodeId));
    }

    /** Undirected endpoint. */
    default void addEndpointUndirected(String nodeId) {
        addEndpoint(ep -> ep.direction(CjDirection.UNDIR).node(nodeId));
    }

    void attachEndpoint(ICjEndpoint endpoint);

    /** Create a not-yet attached endpoint */
    void createEndpoint(Consumer<ICjEndpointMutable> endpoint);

    void edgeType(ICjEdgeType edgeType);

}
