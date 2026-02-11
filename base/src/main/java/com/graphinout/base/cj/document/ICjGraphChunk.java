package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.util.Comparables;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunk extends ICjHasId, ICjHasData, ICjHasLabel {

    static int compare(ICjGraphChunk a, ICjGraphChunk b) {
        return Comparables.<ICjGraphChunk>comparing() //
                .byKey(ICjHasId::id) //
                .byKey(ICjGraphChunk::label) //
                .byKey(ICjGraphChunk::data) //
                .compare(a, b);
    }

    default void copyTo(ICjGraphChunkMutable target) {
        ifPresentAccept(id(), target::id);
        ifPresentAccept(label(), sourceLabel -> target.labelMutable(sourceLabel::copyTo));
        data(data -> target.dataJsonValue(data.jsonValue()));
    }

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.graphStart();
        cjWriter.maybe(id(), cjWriter::id);
        fireDataMaybe(cjWriter);
        fireLabelMaybe(cjWriter, sort);
    }

    /**
     * A hash based on label and data. Excludes id.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (label() != null) {
            sb.append("L:").append(label().structuralHash());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        return Integer.toString(sb.toString().hashCode());
    }

}
