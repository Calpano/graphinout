package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.graphml.GraphmlWriter;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class CjElement implements ICjElement {

    public final @Nullable CjElement parent;
    private boolean isStarted;

    CjElement(@Nullable CjElement parent) {
        this.parent = parent;
        this.isStarted = false;
    }

    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Mark we wrote the START of this element to the downstream {@link GraphmlWriter}.
     */
    public void markAsStarted() {
        isStarted = true;
    }

    // TODO remove?
    public void maybeWriteStartTo(ICjWriter cjWriter) throws IOException {
        if (isStarted()) return;
        if (parent != null) {
            parent.maybeWriteStartTo(cjWriter);
        }
        cjType().fireStart(cjWriter);
        markAsStarted();
    }

    @Nullable
    @Override
    public ICjElement parent() {
        return parent;
    }

    // TODO remove?
    public void writeEndTo(ICjWriter cjWriter) throws IOException {
        maybeWriteStartTo(cjWriter);
        cjType().fireEnd(cjWriter);
    }

}
