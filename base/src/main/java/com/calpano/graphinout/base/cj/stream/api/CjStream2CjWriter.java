package com.calpano.graphinout.base.cj.stream.api;

import com.calpano.graphinout.base.cj.element.ICjDocumentChunk;
import com.calpano.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjEdgeChunk;
import com.calpano.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphChunk;
import com.calpano.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjNodeChunk;
import com.calpano.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjEdgeElement;
import com.calpano.graphinout.base.cj.element.impl.CjGraphElement;
import com.calpano.graphinout.base.cj.element.impl.CjNodeElement;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

public class CjStream2CjWriter implements ICjStream {

    private final ICjWriter cjWriter;

    public CjStream2CjWriter(ICjWriter cjWriter) {this.cjWriter = cjWriter;}

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return new CjDocumentElement();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return new CjEdgeElement(null);
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return new CjGraphElement(null);
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return new CjNodeElement(null);
    }

    @Override
    public void documentEnd() {
        cjWriter.documentEnd();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        document.fireStartChunk(cjWriter);
    }

    @Override
    public void edgeEnd() {
        cjWriter.edgeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        edge.fireStartChunk(cjWriter);
    }

    @Override
    public void graphEnd() {
        cjWriter.graphEnd();
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        graph.fireStartChunk(cjWriter);
    }

    @Override
    public void nodeEnd() {
        cjWriter.nodeEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        node.fireStartChunk(cjWriter);
    }

}
