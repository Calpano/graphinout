package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.stream.ICjWriter;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunk extends ICjHasId, ICjHasData, ICjHasLabel {

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.graphStart();
        cjWriter.maybe(id(), cjWriter::id);
        fireDataMaybe(cjWriter);
        fireLabelMaybe(cjWriter);
    }

}
