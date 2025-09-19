package com.calpano.graphinout.base.cj.element;

import java.util.stream.Stream;

public interface ICjGraph extends ICjGraphChunk, ICjHasGraphs {

    Stream<ICjEdge> edges();

    Stream<ICjNode> nodes();

}
