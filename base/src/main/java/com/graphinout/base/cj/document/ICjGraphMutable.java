package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjGraphMutable extends ICjGraph, ICjHasGraphsMutable, ICjGraphChunkMutable {

    void addEdge(Consumer<ICjEdgeMutable> edge);

    void addNode(Consumer<ICjNodeMutable> node);


}
