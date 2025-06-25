package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface CjEndpoint {

    /**
     * @return the node this endpoint connects to
     */
    String node();

    @Nullable
    String port();

    /**
     * @return the direction of this endpoint (default: undir)
     */
    CjDirection direction();

    @Nullable
    CjEdgeType type();

}
