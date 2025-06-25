package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface CjEdge {

    @Nullable
    String id();

    /**
     * @return true if this edge is directed (default: true)
     */
    default boolean isDirected() {
        return true;
    }

    @Nullable
    CjLabel label();

    @Nullable
    CjEdgeType type();

}
