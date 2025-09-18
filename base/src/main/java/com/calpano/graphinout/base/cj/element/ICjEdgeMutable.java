package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjEdgeType;

import java.util.function.Consumer;

public interface ICjEdgeMutable extends ICjEdge, ICjHasIdMutable, ICjHasGraphsMutable, ICjHasLabelMutable, ICjHasDataMutable {

    void addEndpoint(Consumer<ICjEndpointMutable> endpoint);

    void edgeType(CjEdgeType edgeType);

}
