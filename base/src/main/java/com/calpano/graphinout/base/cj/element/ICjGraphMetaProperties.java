package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjGraphMetaProperties {

    @Nullable
    Boolean canonical();

    @Nullable
    Long nodeCountTotal();

    @Nullable
    Long edgeCountTotal();

    @Nullable
    Long nodeCountInGraph();

    @Nullable
    Long edgeCountInGraph();

}
