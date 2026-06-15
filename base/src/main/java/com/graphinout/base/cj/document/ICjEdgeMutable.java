package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;

/**
 * Mutable variant of {@link ICjEdge} used while constructing a CJ document.
 */
public interface ICjEdgeMutable extends ICjEdge, ICjEdgeChunkMutable, ICjHasGraphsMutable, ICjHasLabelMutable {

    @Override
    @NonNull ICjGraphMutable parent();

}
