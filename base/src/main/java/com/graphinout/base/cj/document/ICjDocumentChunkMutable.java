package com.graphinout.base.cj.document;

import java.util.function.Consumer;

public interface ICjDocumentChunkMutable extends  ICjDocumentChunk, ICjHasDataMutable {

    void baseUri(String baseUri);

    void connectedJson(Consumer<ICjDocumentMetaMutable> meta);

}
