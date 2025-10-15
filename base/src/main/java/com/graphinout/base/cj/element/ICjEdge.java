package com.graphinout.base.cj.element;

import java.util.Objects;
import java.util.stream.Stream;

public interface ICjEdge extends ICjEdgeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), endpoints()), graphs());
    }


}
