package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjEdgeElement extends CjHasDataAndLabelElement implements ICjEdgeMutable {

    private final List<ICjEndpoint> endpoints = new ArrayList<>();
    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String id;
    private ICjEdgeType edgeType;

    @Override
    public void addEndpoint(Consumer<ICjEndpointMutable> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement();
        endpoint.accept(endpointElement);
        assert endpointElement.node() != null : "Endpoint must have a node";
        endpoints.add(endpointElement);
    }

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement();
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Override
    public void attachEndpoint(ICjEndpoint endpoint) {
        assert endpoint.node() != null : "Endpoint must have a node";
        endpoints.add(endpoint);
    }

    @Override
    public CjType cjType() {
        return CjType.Edge;
    }

    @Override
    public void createEndpoint(Consumer<ICjEndpointMutable> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement();
        endpoint.accept(endpointElement);
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
