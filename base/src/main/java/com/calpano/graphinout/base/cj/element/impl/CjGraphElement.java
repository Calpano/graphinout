package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjNodeMutable;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjGraphElement extends CjHasDataAndLabelElement implements ICjGraphMutable {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private final List<CjNodeElement> nodes = new ArrayList<>();
    private final List<CjEdgeElement> edges = new ArrayList<>();
    private String id;

    @Override
    public void addEdge(Consumer<ICjEdgeMutable> edge) {
        CjEdgeElement edgeEvent = new CjEdgeElement();
        edge.accept(edgeEvent);
        edges.add(edgeEvent);
    }

    public void addGraph(CjGraphElement graph) {
        graphs.add(graph);
    }

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement();
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Override
    public void addNode(Consumer<ICjNodeMutable> node) {
        CjNodeElement n = new CjNodeElement();
        node.accept(n);
        nodes.add(n);
    }

    @Override
    public CjType cjType() {
        return CjType.Graph;
    }

    @Override
    public Stream<ICjEdge> edges() {
        return edges.stream().map(x -> (ICjEdge) x);
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);

        cjWriter.list(nodes, CjType.ArrayOfNodes, CjNodeElement::fire);
        cjWriter.list(edges, CjType.ArrayOfEdges, CjEdgeElement::fire);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);

        cjWriter.graphEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(String id) {
        this.id = id;
    }

    @Override
    public Stream<ICjNode> nodes() {
        return nodes.stream().map(x -> (ICjNode) x);
    }

    public void removeNode(CjNodeElement node) {
        nodes.remove(node);
    }

}
