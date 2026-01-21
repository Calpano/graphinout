package com.graphinout.base.cj.factory;

import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.document.impl.CjEdgeChunk;
import com.graphinout.base.cj.document.impl.CjGraphChunk;
import com.graphinout.base.cj.document.impl.CjNodeChunk;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;

public class CjFactory implements ICjFactory {

    public ICjDocumentChunkMutable createDocumentChunk() {
        return new CjDocumentElement();
    }

    public ICjEdgeChunkMutable createEdgeChunk() {
        return new CjEdgeChunk();
    }

    public ICjGraphChunkMutable createGraphChunk() {
        return new CjGraphChunk();
    }

    public ICjNodeChunkMutable createNodeChunk() {
        return new CjNodeChunk();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return JavaJsonFactory.INSTANCE;
    }

}
