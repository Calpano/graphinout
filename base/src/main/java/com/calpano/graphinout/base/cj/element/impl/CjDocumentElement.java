package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjDocumentMeta;
import com.calpano.graphinout.base.cj.element.ICjDocumentMetaMutable;
import com.calpano.graphinout.base.cj.element.ICjDocumentMutable;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A CJ document
 */
public class CjDocumentElement extends CjHasDataElement implements ICjDocumentMutable, ICjDocumentChunkMutable {

    /** All directed graphs in this document */
    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String baseUri;
    private @Nullable CjDocumentMetaElement connectedJson;

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement();
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Nullable
    @Override
    public String baseUri() {
        return baseUri;
    }

    @Override
    public void baseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public CjType cjType() {
        return CjType.RootObject;
    }

    @Override
    public void connectedJson(Consumer<ICjDocumentMetaMutable> consumer) {
        if (this.connectedJson == null) {
            this.connectedJson = new CjDocumentMetaElement();
        }
        consumer.accept(connectedJson);
    }

    @Override
    @Nullable
    public ICjDocumentMeta connectedJson() {
        return connectedJson;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);
        cjWriter.documentEnd();
    }


    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }


    public String toCjJsonString() {
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        fire(cj2JsonWriter);
        return json2StringWriter.jsonString();
    }

}
