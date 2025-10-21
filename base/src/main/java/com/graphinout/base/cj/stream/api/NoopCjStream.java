package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.element.impl.CjEdgeElement;
import com.graphinout.base.cj.element.impl.CjGraphElement;
import com.graphinout.base.cj.element.impl.CjNodeElement;

/**
 * A no-op implementation of ICjStream.
 * <p>
 * All event methods do nothing. Factory methods return simple mutable element instances
 * so callers can populate chunks as usual even if the stream ignores them.
 */
public class NoopCjStream implements ICjStream {

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return new CjDocumentElement();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return new CjEdgeElement();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return new CjGraphElement();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return new CjNodeElement();
    }

    @Override
    public void documentEnd() {
        // no-op
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // no-op
    }

    @Override
    public void edgeEnd() {
        // no-op
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        // no-op
    }

    @Override
    public void graphEnd() {
        // no-op
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // no-op
    }

    @Override
    public void nodeEnd() {
        // no-op
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        // no-op
    }
}
