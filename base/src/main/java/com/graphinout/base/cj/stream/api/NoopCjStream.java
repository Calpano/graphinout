package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Locator;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A no-op implementation of ICjStream.
 * <p>
 * All event methods do nothing. Factory methods return simple mutable element instances so callers can populate chunks
 * as usual even if the stream ignores them.
 */
public class NoopCjStream extends CjFactory implements ICjStream {

    @Nullable
    @Override
    public Consumer<ContentError> contentErrorHandler() {
        return null;
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

    @Nullable
    @Override
    public Locator locator() {
        return null;
    }

    @Override
    public void nodeEnd() {
        // no-op
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        // no-op
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        // no-op
    }

    @Override
    public void setLocator(Locator locator) {
        // no-op
    }

}
