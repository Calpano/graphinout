package com.calpano.graphinout.base.cj.element;

import java.util.Objects;
import java.util.stream.Stream;

public interface ICjDocument extends ICjHasGraphs, ICjDocumentChunk {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()), Stream.of(connectedJson())).filter(Objects::nonNull), graphs());
    }

}
