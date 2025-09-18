package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjGraph extends ICjHasId, ICjHasData, ICjHasGraphs, ICjHasLabel {

    Stream<ICjEdge> edges();

    @Nullable
    ICjGraphMeta meta();

    Stream<ICjNode> nodes();

}
