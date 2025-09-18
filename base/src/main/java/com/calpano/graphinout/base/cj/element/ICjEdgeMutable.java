package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.element.impl.CjEndpointElement;

import java.util.function.Consumer;

public interface ICjEdgeMutable extends ICjEdge, ICjHasIdMutable, ICjHasGraphsMutable {

    void addEndpoint(Consumer<CjEndpointElement> endpoint);

    void edgeType(CjEdgeType edgeType);

}
