package com.graphinout.base.cj.document;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Root of a CJ graph representation assembled from GIO events. It aggregates graphs and document-level metadata.
 */
public interface ICjDocument extends ICjHasGraphs, ICjDocumentChunk {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()), Stream.of(connectedJson())).filter(Objects::nonNull), graphs());
    }

}
