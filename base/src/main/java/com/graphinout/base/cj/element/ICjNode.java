package com.graphinout.base.cj.element;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a node in the CJ model, which may have ports and embedded subgraphs, plus associated data/labels.
 */
public interface ICjNode extends ICjNodeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), ports()), graphs());
    }

}
