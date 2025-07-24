package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;

import javax.annotation.Nullable;
import java.util.Set;

public interface IGraphmlEdge extends IGraphmlWithDescElement {

    String ATTRIBUTE_ID = "id";
    String ATTRIBUTE_DIRECTED = "directed";
    String ATTRIBUTE_SOURCE = "source";
    String ATTRIBUTE_SOURCE_PORT = "sourceport";
    String ATTRIBUTE_TARGET = "target";
    String ATTRIBUTE_TARGET_PORT = "targetport";

    static GraphmlEdge.GraphmlEdgeBuilder builder() {
        return new GraphmlEdge.GraphmlEdgeBuilder();
    }

    @Nullable
    Boolean directed();

    @Nullable String id();

    String source();

    @Nullable
    String sourcePort();

    String target();

    @Nullable
    String targetPort();

    default Set<String> builtInAttributes() {
        return Set.of(ATTRIBUTE_ID, ATTRIBUTE_DIRECTED, ATTRIBUTE_SOURCE,
                     ATTRIBUTE_SOURCE_PORT, ATTRIBUTE_TARGET, ATTRIBUTE_TARGET_PORT);
    }

    @Override
    default String tagName() {
        return "edge";
    }

}
