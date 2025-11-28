package com.graphinout.base.cj.factory;

import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.document.impl.CjEdgeElement;
import com.graphinout.base.cj.document.impl.CjGraphElement;
import com.graphinout.base.cj.document.impl.CjNodeElement;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;

public class CjFactory implements ICjFactory {

    public ICjDocumentChunkMutable createDocumentChunk() {
        return new CjDocumentElement();
    }

    public ICjEdgeChunkMutable createEdgeChunk() {
        return new CjEdgeElement();
    }

    public ICjGraphChunkMutable createGraphChunk() {
        return new CjGraphElement();
    }

    public ICjNodeChunkMutable createNodeChunk() {
        return new CjNodeElement();
    }

    @Override
    public IJsonFactory jsonFactory() {
        return JavaJsonFactory.INSTANCE;
    }

}
