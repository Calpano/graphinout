package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjHasGraphsMutable extends ICjHasGraphs {

    /**
     * @param consumer can modify the new graph
     * @return the new graph
     */
    ICjGraphMutable addGraph(Consumer<ICjGraphMutable> consumer);

    default ICjGraphMutable addGraph() {
        return addGraph(graph -> {});
    }

    /**
     * @param graph to be removed
     * @return true if the graph was removed
     */
    boolean removeGraph(ICjGraph graph);


}
