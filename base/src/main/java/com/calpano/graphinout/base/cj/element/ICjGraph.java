package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjGraph extends ICjGraphProperties, ICjElement {

    @Nullable
    ICjData data();

    Stream<ICjEdge> edges();

    Stream<ICjGraph> graphs();

    @Nullable
    ICjLabel label();

    @Nullable
    ICjGraphMeta meta();

    Stream<ICjNode> nodes();

}
