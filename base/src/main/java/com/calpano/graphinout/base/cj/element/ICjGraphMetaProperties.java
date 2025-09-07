package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjGraphMetaProperties {

    @Nullable
    Boolean canonical();

    @Nullable
    Integer nodeCountTotal();

    @Nullable
    Integer edgeCountTotal();

    @Nullable
    Integer nodeCountInGraph();

    @Nullable
    Integer edgeCountInGraph();

}
