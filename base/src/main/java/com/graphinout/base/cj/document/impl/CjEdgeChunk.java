package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjEdgeChunk extends CjHasDataAndLabelElement implements ICjEdgeChunkMutable {

    private final List<ICjEndpoint> endpoints = new ArrayList<>();
    private @Nullable String id;
    private ICjElementType edgeType;

    @Override
    public CjEdgeChunk addEndpoint(Consumer<ICjEndpointMutable> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement();
        endpoint.accept(endpointElement);
        assert endpointElement.node() != null : "Endpoint must have a node";
        endpoints.add(endpointElement);
        return this;
    }

    @Override
    public void attachEndpoint(ICjEndpoint endpoint) {
        assert endpoint.node() != null : "Endpoint must have a node";
        endpoints.add(endpoint);
    }


    @Override
    public void createEndpoint(Consumer<ICjEndpointMutable> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement();
        endpoint.accept(endpointElement);
    }

    @Override
    public ICjElementType edgeType() {
        return edgeType;
    }

    @Override
    public ICjEdgeChunkMutable edgeType(ICjElementType edgeType) {
        this.edgeType = edgeType;
        return this;
    }

    @Override
    public Stream<ICjEndpoint> endpoints() {
        return endpoints.stream().sorted(Comparator.comparing(ICjEndpoint::node));
    }

    @Nullable
    @Override
    public String id() {
        return id;
    }

    @Override
    public CjEdgeChunk id(@Nullable String id) {
        this.id = id;
        return this;
    }

}
