package com.calpano.graphinout.base.cj.stream.api;

import com.calpano.graphinout.base.cj.element.ICjDocumentChunk;
import com.calpano.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjEdgeChunk;
import com.calpano.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphChunk;
import com.calpano.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjNodeChunk;
import com.calpano.graphinout.base.cj.element.ICjNodeChunkMutable;

/**
 * A coarse-granular streaming CJ API. Assumptions: data is small.
 *
 * This is the API that should replace GioWriter.
 */
public interface ICjStream {

    ICjDocumentChunkMutable createDocumentChunk();

    ICjEdgeChunkMutable createEdgeChunk();

    ICjGraphChunkMutable createGraphChunk();

    ICjNodeChunkMutable createNodeChunk();

    void documentEnd();

    void documentStart(ICjDocumentChunk document);

    void edgeEnd();

    /** Start a CJ edge which may contain subgraphs */
    void edgeStart(ICjEdgeChunk edge);

    void graphEnd();

    /** Start a CJ graph which may contain nodes, edges, subgraphs */
    void graphStart(ICjGraphChunk graph);

    void nodeEnd();

    /** Start a CJ node which may contain subgraphs */
    void nodeStart(ICjNodeChunk node);

}
