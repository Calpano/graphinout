package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.Nullable;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunk extends ICjHasId, ICjHasData, ICjHasLabel {

    static int compare(ICjGraphChunk a, ICjGraphChunk b) {
        return Comparables.<ICjGraphChunk>comparing() //
                .byKey(ICjHasId::id) //
                .byKey(ICjGraphChunk::baseUri) //
                .byKey(ICjGraphChunk::label) //
                .byKey(ICjGraphChunk::data) //
                .compare(a, b);
    }

    /**
     * Base URI for resolving relative URIs within this graph (CJ 7.0.0)
     */
    @Nullable String baseUri();

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.graphStart();
        cjWriter.maybe(id(), cjWriter::id);
        cjWriter.maybe(baseUri(), cjWriter::baseUri);
        fireDataMaybe(cjWriter);
        fireLabelMaybe(cjWriter, sort);
    }

}
