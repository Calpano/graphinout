package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjDocumentMeta;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjWithMutableGraphs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A CJ document
 */
public class CjDocumentElement extends CjWithDataElement implements ICjDocument, ICjWithMutableGraphs {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String baseUri;
    private ICjDocumentMeta connectedJson;

    public CjDocumentElement() {
        super(null);
    }

    public CjGraphElement addGraph(Consumer<CjGraphElement> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }

    @Nullable
    @Override
    public String baseUri() {
        return baseUri;
    }

    public void baseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public CjType cjType() {
        return CjType.RootObject;
    }

    @Override
    @Nullable
    public ICjDocumentMeta connectedJson() {
        return connectedJson;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.documentStart();
        if (baseUri != null)
            cjWriter.baseUri(baseUri);
        fireDataMaybe(cjWriter);

        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);

        cjWriter.documentEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    public List<CjGraphElement> graphsMutable() {
        return graphs;
    }

    public void setBaseUri(@Nullable String baseUri) {
        this.baseUri = baseUri;
    }


}
