package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;

import javax.annotation.Nullable;


/**
 * The part of a CJ document which can be sent in one go. Memory requirements for all data in this chunk are expected to
 * be below 50 MB.
 */
public interface ICjDocumentChunk extends ICjChunkMutable, ICjHasData {

    @Nullable
    String baseUri();

    @Nullable
    ICjDocumentMeta connectedJson();

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.documentStart();
        cjWriter.maybe(baseUri(), cjWriter::baseUri);
        cjWriter.maybe(connectedJson(), cj -> cj.fire(cjWriter));
        fireDataMaybe(cjWriter);
    }


}
