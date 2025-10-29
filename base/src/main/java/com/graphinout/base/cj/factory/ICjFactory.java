package com.graphinout.base.cj.factory;

import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.foundation.json.value.IJsonFactory;

public interface ICjFactory {

    ICjDocumentChunkMutable createDocumentChunk();

    ICjEdgeChunkMutable createEdgeChunk();

    ICjGraphChunkMutable createGraphChunk();

    ICjNodeChunkMutable createNodeChunk();

    default ICjNodeChunkMutable createNodeChunkWithId(String id) {
        ICjNodeChunkMutable nodeChunk = createNodeChunk();
        nodeChunk.id(id);
        return nodeChunk;
    }

    IJsonFactory jsonFactory();

}
