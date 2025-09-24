package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjEndpointMutable;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjEdgeElement extends CjHasDataAndLabelElement implements ICjEdgeMutable {

    private final List<ICjEndpointMutable> endpoints = new ArrayList<>();
    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String id;
    private ICjEdgeType edgeType;

    @Override
    public void addEndpoint(Consumer<ICjEndpointMutable> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement();
        endpoint.accept(endpointElement);
        endpoints.add(endpointElement);
    }

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement();
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Override
    public CjType cjType() {
        return CjType.Edge;
    }

    @Override
    public ICjEdgeType edgeType() {
        return edgeType;
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        this.edgeType = edgeType;
    }

    @Override
    public Stream<ICjEndpoint> endpoints() {
        return endpoints.stream().map(x -> (ICjEndpoint) x).sorted(Comparator.comparing(ICjEndpoint::node));
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, ICjGraph::fire);
        cjWriter.edgeEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Nullable
    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(@Nullable String id) {
        this.id = id;
    }

}
