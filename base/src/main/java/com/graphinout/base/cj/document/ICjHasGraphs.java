package com.graphinout.base.cj.document;

import java.util.stream.Stream;

public interface ICjHasGraphs {

    Stream<ICjGraph> graphs();

    /**
     * @return All graphs in the document, including their nested subgraphs, recursively. Including graphs nested in
     * nodes or edges.
     */
    default Stream<ICjGraph> graphsAll() {
        return graphs().flatMap(g -> Stream.concat(Stream.of(g), g.graphsNestedNonRecursive()));
    }

    /** Index of given graph in this element. -1 if not found */
    int indexOf(ICjGraph graph);

}
