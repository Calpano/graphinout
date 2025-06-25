package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface CjNode {

    @Nullable
    String id();

    @Nullable
    CjLabel label();

}
