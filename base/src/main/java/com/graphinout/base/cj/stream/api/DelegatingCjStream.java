package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;

import java.util.Arrays;
import java.util.List;

/**
 * Delegates one input stream to n output streams. The first one needs working factory methods like
 * {@link ICjStream#createDocumentChunk()}
 */
public class DelegatingCjStream implements ICjStream {

    private final List<ICjStream> streams;

    public DelegatingCjStream(ICjStream... streams) {
        this.streams = Arrays.asList(streams);
    }

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        // create via first stream and return; others will receive the passed chunk in documentStart
        return streams.getFirst().createDocumentChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return streams.getFirst().createEdgeChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return streams.getFirst().createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return streams.getFirst().createNodeChunk();
    }

    @Override
    public void documentEnd() {
        for (ICjStream stream : streams) {
            stream.documentEnd();
        }
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        for (ICjStream stream : streams) {
            stream.documentStart(document);
        }
    }

    @Override
    public void edgeEnd() {
        for (ICjStream stream : streams) {
            stream.edgeEnd();
        }
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        for (ICjStream stream : streams) {
            stream.edgeStart(edge);
        }
    }

    @Override
    public void graphEnd() {
        for (ICjStream stream : streams) {
            stream.graphEnd();
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        for (ICjStream stream : streams) {
            stream.graphStart(graph);
        }
    }

    @Override
    public void nodeEnd() {
        for (ICjStream stream : streams) {
            stream.nodeEnd();
        }
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        for (ICjStream stream : streams) {
            stream.nodeStart(node);
        }
    }

}
