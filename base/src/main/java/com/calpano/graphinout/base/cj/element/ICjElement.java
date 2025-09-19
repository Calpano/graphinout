package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;

public interface ICjElement {

    default ICjDocumentMutable asDocument() {
        return (ICjDocumentMutable) this;
    }

    default ICjEdgeMutable asEdge() {
        return (ICjEdgeMutable) this;
    }

    default ICjEndpointMutable asEndpoint() {
        return (ICjEndpointMutable) this;
    }

    default ICjGraphMutable asGraph() {
        return (ICjGraphMutable) this;
    }

    default ICjNodeMutable asNode() {
        return (ICjNodeMutable) this;
    }

    default ICjPortMutable asPort() {
        return (ICjPortMutable) this;
    }

    default ICjHasDataMutable asWithData() {
        return (ICjHasDataMutable) this;
    }

    CjType cjType();

    void fire(ICjWriter cjWriter);

    @Nullable
    ICjElement parent();

}
