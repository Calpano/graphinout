package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

public interface ICjHasGraphsMutable extends ICjHasGraphs {

    void addGraph(Consumer<ICjGraphMutable> consumer);

}
