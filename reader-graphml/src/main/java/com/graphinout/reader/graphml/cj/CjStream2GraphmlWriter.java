package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.BaseCjOutput;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.reader.graphml.GraphmlWriter;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.json.value.IJsonFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public class CjStream2GraphmlWriter extends BaseCjOutput implements ICjStream {

    private final CjStream2CjWriter cjStream2CjWriter;

    public CjStream2GraphmlWriter(GraphmlWriter graphmlWriter) {
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter((cjDoc) -> {
            try {
                CjDocument2Graphml.writeToGraphml(cjDoc, graphmlWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.cjStream2CjWriter = new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return cjStream2CjWriter.createDocumentChunk();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return cjStream2CjWriter.createEdgeChunk();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return cjStream2CjWriter.createGraphChunk();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return cjStream2CjWriter.createNodeChunk();
    }

    @Override
    public void documentEnd() {
        cjStream2CjWriter.documentEnd();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        cjStream2CjWriter.documentStart(document);
    }

    @Override
    public void edgeEnd() {
        cjStream2CjWriter.edgeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        cjStream2CjWriter.edgeStart(edge);
    }

    @Override
    public void graphEnd() {
        cjStream2CjWriter.graphEnd();
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        cjStream2CjWriter.graphStart(graph);
    }

    @Override
    public IJsonFactory jsonFactory() {
        return cjStream2CjWriter.jsonFactory();
    }

    @Override
    public void nodeEnd() {
        cjStream2CjWriter.nodeEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        cjStream2CjWriter.nodeStart(node);
    }

    @Override
    public void setContentErrorHandler(@Nullable Consumer<ContentError> contentErrorHandler) {
        super.setContentErrorHandler(contentErrorHandler);
        cjStream2CjWriter.setContentErrorHandler(contentErrorHandler);
    }

    @Override
    public void setLocator(@Nullable Locator locator) {
        super.setLocator(locator);
        cjStream2CjWriter.setLocator(locator);
    }


}
