package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjEdge extends ICjEdgeProperties {

    @Nullable
    JsonNode data();

    List<ICjEndpoint> endpoints();

    List<ICjGraph> graphs();

    @Nullable
    ICjLabel label();


}
