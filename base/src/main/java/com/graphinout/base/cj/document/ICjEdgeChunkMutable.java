package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjEdgeChunkMutable extends ICjChunkMutable, ICjEdgeChunk, ICjHasIdMutable, ICjHasLabelMutable, ICjHasDataMutable {

    /** Create a not-yet attached endpoint */
    void createEndpoint(Consumer<ICjEndpointMutable> endpoint);

    void attachEndpoint(ICjEndpoint endpoint);

    /** Consumer should finish with a valid endpoint. */
    void addEndpoint(Consumer<ICjEndpointMutable> endpoint);

    void edgeType(ICjEdgeType edgeType);

}
