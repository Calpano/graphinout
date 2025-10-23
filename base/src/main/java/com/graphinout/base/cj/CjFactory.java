package com.graphinout.base.cj;

import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.element.impl.CjEdgeElement;
import com.graphinout.base.cj.element.impl.CjGraphElement;
import com.graphinout.base.cj.element.impl.CjNodeElement;
import com.graphinout.base.cj.stream.api.ICjFactory;
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
