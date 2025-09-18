package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjGraphMeta extends ICjElement {

    @Nullable
    Boolean canonical();

    @Nullable
    Long edgeCountInGraph();

    @Nullable
    Long edgeCountTotal();

    @Nullable
    Long nodeCountInGraph();

    @Nullable
    Long nodeCountTotal();

}
