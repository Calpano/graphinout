package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;

public interface ICjEdgeMutable extends ICjEdge, ICjEdgeChunkMutable, ICjHasGraphsMutable, ICjHasLabelMutable {

    @Override
    @NonNull ICjGraphMutable parent();

}
