package com.calpano.graphinout.base.cj;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface CjPort {

    @Nullable
    String id();

    @Nullable
    CjLabel label();

}
