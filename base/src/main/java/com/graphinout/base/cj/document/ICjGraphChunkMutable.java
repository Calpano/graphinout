package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.annotations.ModificationOperation;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunkMutable extends ICjChunkMutable, ICjGraphChunk, ICjHasIdMutable<ICjGraphChunkMutable>, ICjHasDataMutable, ICjHasLabelMutable {

    /**
     * Set the base URI for this graph (CJ 7.0.0)
     */
    @ModificationOperation
    void baseUri(String baseUri);

}
