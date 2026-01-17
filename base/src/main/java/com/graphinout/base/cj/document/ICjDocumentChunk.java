package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;


/**
 * The part of a CJ document which can be sent in one go. Memory requirements for all data in this chunk are expected to
 * be below 50 MB.
 */
public interface ICjDocumentChunk extends ICjChunkMutable, ICjHasData {

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

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.documentStart();
        cjWriter.maybe(baseUri(), cjWriter::baseUri);
        cjWriter.maybe(connectedJson(), cj -> cj.fire(cjWriter));
        fireDataMaybe(cjWriter);
    }

    /**
     * Combine baseUri with localName to a full URI. If the baseUri ends with a slash, concatenate. Otherwise, insert a
     * hash mark. This behavior reflects the typical RDF vocabularies.
     * <p>
     * TODO add this to CJ spec
     *
     * @param localName the local name, should not start with slash or hash mark, but an alpha numeric
     * @return a URI composed of baseUri and localName
     */
    default String uri(String localName) {
        String base = nonNullOrDefault(baseUri(), "");
        if (!base.endsWith("/")) {
            base += "#";
        }
        return base + localName;
    }

}
