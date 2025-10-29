package com.graphinout.base.cj.document;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents an edge between endpoints in the CJ model, optionally containing nested subgraphs.
 */
public interface ICjEdge extends ICjEdgeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), endpoints()), graphs());
    }


}
