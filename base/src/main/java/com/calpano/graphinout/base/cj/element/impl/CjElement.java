package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
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

    public CjHasDataElement asWithData() {
        return (CjHasDataElement) this;
    }

    public CjDocumentElement asDocument() {
        return (CjDocumentElement) this;
    }

    public CjEdgeElement asEdge() {
        return (CjEdgeElement) this;
    }

    public CjEndpointElement asEndpoint() {
        return (CjEndpointElement) this;
    }

    public ICjGraphMutable asGraph() {
        return (CjGraphElement) this;
    }

    public CjNodeElement asNode() {
        return (CjNodeElement) this;
    }

    public CjPortElement asPort() {
        return (CjPortElement) this;
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
    public void maybeWriteStartTo(CjWriter cjWriter) throws IOException {
        if (isStarted()) return;
        if (parent != null) {
            parent.maybeWriteStartTo(cjWriter);
        }
        cjType().fireStart(cjWriter);
        markAsStarted();
    }

    // TODO remove?
    public void writeEndTo(CjWriter cjWriter) throws IOException {
        maybeWriteStartTo(cjWriter);
        cjType().fireEnd(cjWriter);
    }

}
