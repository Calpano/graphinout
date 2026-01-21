package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjGraphChunkMutable;

public class CjGraphChunk extends CjHasDataAndLabelElement implements ICjGraphChunkMutable {

    private String id;
    private String baseUri;

    @Override
    public String baseUri() {
        return baseUri;
    }

    @Override
    public void baseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CjGraphChunk id(String id) {
        this.id = id;
        return this;
    }


}
