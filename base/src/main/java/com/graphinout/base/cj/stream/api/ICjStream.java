package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;

/**
 * A coarse-granular streaming CJ API. Assumptions: data is small.
 * <p>
 * This is the API that should replace GioWriter.
 */
public interface ICjStream {

    ICjDocumentChunkMutable createDocumentChunk();

    ICjEdgeChunkMutable createEdgeChunk();

    ICjGraphChunkMutable createGraphChunk();

    ICjNodeChunkMutable createNodeChunk();

    void documentEnd();

    /** Next expect: 0...n graphs */
    void documentStart(ICjDocumentChunk document);

    void edgeEnd();

    /** Start a CJ edge which may contain subgraphs */
    void edgeStart(ICjEdgeChunk edge);

    default void edge(ICjEdgeChunk edgeChunk) {
        edgeStart(edgeChunk);
        edgeEnd();
    }

    void graphEnd();

    /** Start a CJ graph which may contain nodes, edges, subgraphs */
    void graphStart(ICjGraphChunk graph);

    void nodeEnd();

    /** Start a CJ node which may contain subgraphs */
    void nodeStart(ICjNodeChunk node);

}
