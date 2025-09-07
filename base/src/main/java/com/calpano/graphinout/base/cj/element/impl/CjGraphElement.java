package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjGraphProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CjGraphElement extends CjWithDataElement implements ICjGraphProperties, ICjElement {

    private String id;
    private List<CjEdgeElement> edges = new ArrayList<>();

    CjGraphElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    public CjEdgeElement addEdge(Consumer<CjEdgeElement> edge) {
        CjEdgeElement edgeEvent = new CjEdgeElement(this);
        edge.accept(edgeEvent);
        edges.add(edgeEvent);
        return edgeEvent;
    }

    @Override
    public CjType cjType() {
        return CjType.Graph;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.graphStart();
        cjWriter.id(id);
        // TODO meta

        fireDataMaybe(cjWriter);
        edges.forEach(cjEdgeElement -> cjEdgeElement.fire(cjWriter));
        cjWriter.graphEnd();
    }

    @Override
    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

}
