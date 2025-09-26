package com.calpano.graphinout.base.cj.element;

import java.util.Objects;
import java.util.stream.Stream;

public interface ICjNode extends ICjNodeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), ports()), graphs());
    }

}
