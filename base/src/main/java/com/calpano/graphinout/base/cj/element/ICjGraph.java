package com.calpano.graphinout.base.cj.element;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjGraph extends ICjGraphProperties {

    @Nullable
    ICjGraphMeta meta();

    @Nullable
    ICjLabel label();

    @Nullable
    JsonNode data();

    List<ICjNode> nodes();

    List<ICjEdge> edges();

    List<ICjGraph> graphs();

}
