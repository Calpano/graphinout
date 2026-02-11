package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;


/**
 * The part of a CJ document which can be sent in one go. Memory requirements for all data in this chunk are expected to
 * be below 50 MB.
 */
public interface ICjDocumentChunk extends ICjChunkMutable, ICjHasData {

    /**
     * Abbreviate a URI to a prefixed ID using the document {@code @context}.
     */
    default @Nullable String asId(@Nullable String uri) {
        if (uri == null) {
            return null;
        }
        return asId_(uri);
    }

    default @NonNull String asId_(@NonNull String uri) {
        Map<String, String> ctx = context();
        return CjUris.abbreviateUri(ctx, uri);
    }

    @Nullable ICjDocumentMeta connectedJson();

    /**
     * The document-level {@code @context} namespace map for URI expansion.
     */
    @Nullable Map<String, String> context();

    default ICjDocumentChunkMutable copyMutable() {
        CjDocumentElement copy = new CjDocumentElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjDocumentChunkMutable doc) {
        ifPresentAccept(context(), doc::context);
        ifPresentAccept(connectedJson(), ICjDocumentMeta::copyMutable, doc::connectedJson);
    }

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.documentStart();
        cjWriter.maybe(context(), cjWriter::context);
        ICjDocumentMeta connectedJson = nonNullOrDefault(connectedJson(), CjConstants.DEFAULT_META);
        cjWriter.maybe(connectedJson, cj -> cj.fire(cjWriter, sort));
        fireDataMaybe(cjWriter);
    }

    /**
     * A hash based on data and connectedJson. Excludes context.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (connectedJson() != null) {
            sb.append("CJ:").append(connectedJson().hashCode());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        return Integer.toString(sb.toString().hashCode());
    }

}
