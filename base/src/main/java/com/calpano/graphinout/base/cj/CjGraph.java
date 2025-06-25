package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface CjGraph {

    @Nullable
    String baseuri();

    @Nullable
    String edgedefault();

    @Nullable
    String id();

    @Nullable
    CjLabel label();

}
