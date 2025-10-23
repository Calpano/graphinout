package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
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
