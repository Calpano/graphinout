package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjEdgeTypeSource;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjEdgeProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CjEdgeElement extends CjWithDataElement implements ICjEdgeProperties, ICjElement {

    private @Nullable String id;
    private @Nullable String type;
    private @Nullable String typeUri;
    private @Nullable String typeNode;
    private final List<CjEndpointElement> endpoints = new ArrayList<>();

    CjEdgeElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    public void addEndpoint(Consumer<CjEndpointElement> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement(this);
        endpoint.accept(endpointElement);
        endpoints.add(endpointElement);
    }

    @Override
    public CjType cjType() {
        return CjType.Edge;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.edgeStart();

        // FIXME code to select from 3 given values according to precedence
        CjEdgeTypeSource cjEdgeTypeSource = CjEdgeTypeSource.Node;
        String edgeType = typeNode;
        cjWriter.edgeType(CjEdgeType.of(cjEdgeTypeSource, edgeType));

        endpoints.forEach(cjEndpointElement -> cjEndpointElement.fire(cjWriter));

        fireDataMaybe(cjWriter);

        cjWriter.edgeEnd();
    }


    @Nullable
    @Override
    public String id() {
        return id;
    }

    public void id(@Nullable String id) {
        this.id = id;
    }

    public void type(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    @Override
    public String type() {
        return type;
    }

    public void typeNode(@Nullable String typeNode) {
        this.typeNode = typeNode;
    }

    @Nullable
    @Override
    public String typeNode() {
        return typeNode;
    }

    public void typeUri(@Nullable String typeUri) {
        this.typeUri = typeUri;
    }

    @Nullable
    @Override
    public String typeUri() {
        return typeUri;
    }

}
