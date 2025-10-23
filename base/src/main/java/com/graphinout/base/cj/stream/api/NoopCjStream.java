package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;

/**
 * A no-op implementation of ICjStream.
 * <p>
 * All event methods do nothing. Factory methods return simple mutable element instances so callers can populate chunks
 * as usual even if the stream ignores them.
 */
public class NoopCjStream extends CjFactory implements ICjStream {

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
