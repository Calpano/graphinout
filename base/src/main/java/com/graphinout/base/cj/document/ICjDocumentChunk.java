package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;


/**
 * The part of a CJ document which can be sent in one go. Memory requirements for all data in this chunk are expected to
 * be below 50 MB.
 */
public interface ICjDocumentChunk extends ICjChunkMutable, ICjHasData {

    default @Nullable String asId(@Nullable String uri) {
        if (uri == null) {
            return null;
        }
        return asId_(uri);
    }

    default @NonNull String asId_(@NonNull String uri) {
        String baseUri = baseUri();
        if (baseUri == null) {
            return uri;
        }
        int index = uri.indexOf(baseUri);
        if (index == -1) {
            return uri;
        }
        return uri.substring(index + baseUri.length());
    }

    @Nullable String baseUri();

    @Nullable ICjDocumentMeta connectedJson();

    default ICjDocumentChunkMutable copyMutable() {
        CjDocumentElement copy = new CjDocumentElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjDocumentChunkMutable doc) {
        doc.baseUri(baseUri());
        ifPresentAccept(connectedJson(), ICjDocumentMeta::copyMutable, doc::connectedJson);
    }

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.documentStart();
        cjWriter.maybe(baseUri(), cjWriter::baseUri);
        cjWriter.maybe(connectedJson(), cj -> cj.fire(cjWriter, sort));
        fireDataMaybe(cjWriter);
    }


}
