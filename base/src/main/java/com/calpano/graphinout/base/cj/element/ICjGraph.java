package com.calpano.graphinout.base.cj.element;

import java.util.Objects;
import java.util.stream.Stream;

public interface ICjGraph extends ICjGraphChunk, ICjHasGraphs {

    /** Edge count in this graph, excluding subgraphs */
    default long countEdgesDirect() {
        return edges().count();
    }

    /** Node count in this graph, excluding subgraphs */
    default long countNodesDirect() {
        return nodes().count();
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat( //
                Stream.concat( //
                        Stream.of(data()).filter(Objects::nonNull), //
                        Stream.concat(nodes(), edges()) //
                ),//
                graphs());
    }

    Stream<ICjEdge> edges();

    Stream<ICjNode> nodes();


}
