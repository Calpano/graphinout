package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.ICjEdgeType;

import java.util.function.Consumer;

public interface ICjEdgeMutable extends ICjEdge, ICjHasIdMutable, ICjHasGraphsMutable, ICjHasLabelMutable, ICjHasDataMutable {

    void addEndpoint(Consumer<ICjEndpointMutable> endpoint);

    void edgeType(ICjEdgeType edgeType);

}
