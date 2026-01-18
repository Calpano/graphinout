package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjEdgeChunkMutable extends ICjChunkMutable, ICjEdgeChunk, ICjHasIdMutable<ICjEdgeChunkMutable>, ICjHasLabelMutable, ICjHasDataMutable {

    /** Consumer should finish with a valid endpoint. */
    ICjEdgeChunkMutable addEndpoint(Consumer<ICjEndpointMutable> endpoint);

    /** Incoming endpoint (from the perspective of the edge) */
    default ICjEdgeChunkMutable addEndpointIncoming(String nodeId) {
        return addEndpoint(ep -> ep.direction(CjDirection.IN).node(nodeId));
    }

    /** Outgoing endpoint (from the perspective of the edge) */
    default ICjEdgeChunkMutable addEndpointOutgoing(String nodeId) {
        return addEndpoint(ep -> ep.direction(CjDirection.OUT).node(nodeId));
    }

    /** Undirected endpoint. */
    default ICjEdgeChunkMutable addEndpointUndirected(String nodeId) {
        return addEndpoint(ep -> ep.direction(CjDirection.UNDIR).node(nodeId));
    }

    void attachEndpoint(ICjEndpoint endpoint);

    /** Create a not-yet attached endpoint */
    void createEndpoint(Consumer<ICjEndpointMutable> endpoint);

    ICjEdgeChunkMutable edgeType(ICjElementType edgeType);

}
