package com.graphinout.base.cj.factory;

import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;

/**
 * Factory for creating mutable CJ chunk instances (document, graph, node, edge, ...) while streaming events.
 */
public interface ICjFactory {

    /**
     * @return a new mutable CJ document chunk.
     */
    ICjDocumentChunkMutable createDocumentChunk();

    /**
     * @return a new mutable CJ edge chunk.
     */
    ICjEdgeChunkMutable createEdgeChunk();

    /**
     * @return a new mutable CJ graph chunk.
     */
    ICjGraphChunkMutable createGraphChunk();

    /**
     * @return a new mutable CJ node chunk.
     */
    ICjNodeChunkMutable createNodeChunk();

    /**
     * Creates a new mutable CJ node chunk with the specified ID.
     *
     * @param id the ID for the new node.
     * @return the created node chunk.
     */
    default ICjNodeChunkMutable createNodeChunkWithId(String id) {
        ICjNodeChunkMutable nodeChunk = createNodeChunk();
        nodeChunk.id(id);
        return nodeChunk;
    }

    /**
     * @return the JSON factory associated with this CJ factory.
     */
    IJsonFactory jsonFactory();

}
