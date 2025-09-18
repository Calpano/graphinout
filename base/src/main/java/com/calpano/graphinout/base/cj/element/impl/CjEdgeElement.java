package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjHasGraphsMutable;
import com.calpano.graphinout.base.cj.element.ICjHasIdMutable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class CjEdgeElement extends CjHasDataAndLabelElement implements ICjEdge, ICjHasIdMutable, ICjHasGraphsMutable {

    private final List<CjEndpointElement> endpoints = new ArrayList<>();
    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String id;
    private CjEdgeType edgeType;

    CjEdgeElement(@Nullable CjHasDataElement parent) {
        super(parent);
    }

    public void addEndpoint(Consumer<CjEndpointElement> endpoint) {
        CjEndpointElement endpointElement = new CjEndpointElement(this);
        endpoint.accept(endpointElement);
        endpoints.add(endpointElement);
    }

    public CjGraphElement addGraph(Consumer<CjGraphElement> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }

    @Override
    public CjType cjType() {
        return CjType.Edge;
    }

    public CjEdgeType edgeType() {
        return edgeType;
    }

    public void edgeType(CjEdgeType edgeType) {
        this.edgeType = edgeType;
    }

    @Override
    public Stream<ICjEndpoint> endpoints() {
        return endpoints.stream().map(x -> (ICjEndpoint) x);
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.edgeStart();
        // streaming order: id, label, type, typeUri, typeNode, endpoints, data, graphs
        cjWriter.maybe(id, cjWriter::id);
        fireLabelMaybe(cjWriter);
        ofNullable(edgeType()).ifPresent(cjWriter::edgeType);
        cjWriter.list(endpoints, CjType.ArrayOfEndpoints, CjEndpointElement::fire);
        fireDataMaybe(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);

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

    public void id(@Nullable String id) {
        this.id = id;
    }

}
