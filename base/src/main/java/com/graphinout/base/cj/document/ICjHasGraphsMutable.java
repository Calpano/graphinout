package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjHasGraphsMutable extends ICjHasGraphs {

    void addGraph(Consumer<ICjGraphMutable> consumer);

}
