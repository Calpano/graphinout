package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjGraphMutable extends ICjGraph, ICjHasIdMutable, ICjHasGraphsMutable, ICjHasLabelMutable, ICjHasDataMutable {

    void addEdge(Consumer<ICjEdgeMutable> edge);

    void addNode(Consumer<ICjNodeMutable> node);

    void meta(Consumer<ICjGraphMetaMutable> meta);

}
