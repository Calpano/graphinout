package com.calpano.graphinout.base.cj.element;

import java.util.function.Consumer;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunkMutable extends ICjGraphChunk, ICjHasIdMutable, ICjHasDataMutable, ICjHasLabelMutable {

    void meta(Consumer<ICjGraphMetaMutable> meta);

}
