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

}
