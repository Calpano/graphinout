package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.annotations.ModificationOperation;

import java.util.function.Consumer;

public interface ICjDocumentChunkMutable extends ICjDocumentChunk, ICjHasDataMutable {

    @ModificationOperation
    void baseUri(String baseUri);

    /** A simple setter */
    @ModificationOperation
    void connectedJson(ICjDocumentMetaMutable meta);

    /** Creates a new {@link ICjDocumentMetaMutable}, attaches it, and lets the consumer modify it */
    @ModificationOperation
    void connectedJson(Consumer<ICjDocumentMetaMutable> meta);

}
