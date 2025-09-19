package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;


/**
 * The part of a CJ document which can be sent in one go. Memory requirements for all data in this chunk are expected to
 * be below 50 MB.
 */
public interface ICjDocumentChunk extends ICjHasData {

    @Nullable
    String baseUri();

    @Nullable
    ICjDocumentMeta connectedJson();

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.documentStart();
        ofNullable(baseUri()).ifPresent(cjWriter::baseUri);
        fireDataMaybe(cjWriter);
    }


}
