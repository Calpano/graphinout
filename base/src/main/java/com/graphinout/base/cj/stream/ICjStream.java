package com.graphinout.base.cj.stream;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.factory.ICjFactory;
import com.graphinout.foundation.pure.input.IHandleContentErrors;
import com.graphinout.foundation.pure.json.document.IJsonFactory;

import java.util.function.Consumer;

/**
 * A coarse-granular streaming CJ API. Assumptions: data is small.
 * <p>
 * This is the API that should replace GioWriter.
 */
public interface ICjStream extends ICjFactory, IHandleContentErrors {

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

    /**
     * Creates a new {@link ICjEdgeChunkMutable}, let the {@code edgeCustomizer} attach more data to it, and then adds
     * it to the CJ stream.
     *
     * @param edgeCustomizer
     */
    default void edge(Consumer<ICjEdgeChunkMutable> edgeCustomizer) {
        ICjEdgeChunkMutable edge = createEdgeChunk();
        edgeCustomizer.accept(edge);
        edge(edge);
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

    /**
     * Creates a new {@link ICjNodeChunkMutable}, let the {@code nodeCustomizer} attach more data to it, and then adds
     * it to the CJ stream.
     *
     * @param nodeCustomizer
     */
    default void node(Consumer<ICjNodeChunkMutable> nodeCustomizer) {
        ICjNodeChunkMutable node = createNodeChunk();
        nodeCustomizer.accept(node);
        node(node);
    }

    default void node(ICjNodeChunk node) {
        nodeStart(node);
        nodeEnd();
    }

    void nodeEnd();

    /** Start a CJ node which may contain subgraphs */
    void nodeStart(ICjNodeChunk node);

}
