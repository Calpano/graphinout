package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.foundation.json.value.IJsonFactory;

/**
 * A coarse-granular streaming CJ API. Assumptions: data is small.
 * <p>
 * This is the API that should replace GioWriter.
 */
public interface ICjStream extends ICjFactory {

    default void document(ICjDocumentChunk document) {
        documentStart(document);
        documentEnd();
    }

    void documentEnd();

    /** Next expect: 0...n graphs */
    void documentStart(ICjDocumentChunk document);

    default void edge(ICjEdgeChunk edgeChunk) {
        edgeStart(edgeChunk);
        edgeEnd();
    }

    void edgeEnd();

    /** Start a CJ edge which may contain subgraphs */
    void edgeStart(ICjEdgeChunk edge);

    default void graph(ICjGraphChunk graph) {
        graphStart(graph);
        graphEnd();
    }

    void graphEnd();

    /** Start a CJ graph which may contain nodes, edges, subgraphs */
    void graphStart(ICjGraphChunk graph);

    IJsonFactory jsonFactory();

    default void node(ICjNodeChunk node) {
        nodeStart(node);
        nodeEnd();
    }

    void nodeEnd();

    /** Start a CJ node which may contain subgraphs */
    void nodeStart(ICjNodeChunk node);

}
