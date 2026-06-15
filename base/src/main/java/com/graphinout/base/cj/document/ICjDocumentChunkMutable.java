package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.annotations.ModificationOperation;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Mutable variant of {@link ICjDocumentChunk} used while constructing a CJ document.
 */
public interface ICjDocumentChunkMutable extends ICjDocumentChunk, ICjHasDataMutable {

    /**
     * Set the document-level {@code @context} namespace map.
     */
    @ModificationOperation
    void context(Map<String, String> context);

    /** A simple setter */
    @ModificationOperation
    void connectedJson(@Nullable ICjDocumentMeta meta);

    /** Creates a new {@link ICjDocumentMetaMutable}, attaches it, and lets the consumer modify it */
    @ModificationOperation
    void connectedJson(Consumer<ICjDocumentMetaMutable> meta);


}
