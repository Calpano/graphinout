package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjDocumentProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A CJ document
 */
public class CjDocumentElement extends CjWithDataElement implements ICjDocumentProperties, ICjElement {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String baseUri;

    public CjDocumentElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    public CjElement addGraph(Consumer<CjGraphElement> graph) {
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

    @Override
    public CjType cjType() {
        return CjType.RootObject;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.documentStart();
        cjWriter.baseUri(baseUri);
        fireDataMaybe(cjWriter);
        graphs.forEach(graph -> graph.fire(cjWriter));
        cjWriter.documentEnd();
    }


    public void setBaseUri(@Nullable String baseUri) {
        this.baseUri = baseUri;
    }

}
