package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjCoreElement;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjGraphElement extends CjGraphChunk implements ICjGraphMutable {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private final List<CjNodeElement> nodes = new ArrayList<>();
    private final List<CjEdgeElement> edges = new ArrayList<>();
    private final @NonNull ICjElement parent;

    /**
     * @param parent must be an {@link ICjDocument} or {@link ICjCoreElement}
     */
    public CjGraphElement(@NonNull ICjElement parent) {this.parent = parent;}


    @Override
    public CjEdgeElement addEdge(Consumer<ICjEdgeMutable> edge) {
        CjEdgeElement edgeEvent = new CjEdgeElement(this);
        edge.accept(edgeEvent);
        edges.add(edgeEvent);
        return edgeEvent;
    }

    public void addGraph(CjGraphElement graph) {
        graphs.add(graph);
    }

    @Override
    public CjGraphElement addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }

    @Override
    public CjNodeElement addNode(Consumer<ICjNodeMutable> node) {
        CjNodeElement n = new CjNodeElement(this);
        node.accept(n);
        nodes.add(n);
        return n;
    }

    @Override
    public CjType cjType() {
        return CjType.Graph;
    }

    @Override
    public Stream<ICjEdge> edges() {
        //noinspection RedundantCast
        return edges.stream().map(x -> (ICjEdge) x);
    }

    @Override
    public void fire(ICjWriter cjWriter, boolean sort) {
        fireStartChunk(cjWriter, sort);

        cjWriter.list(nodes, CjType.ArrayOfNodes, sort, (cjNodeElement, cjWriter1) -> cjNodeElement.fire(cjWriter1, sort));
        cjWriter.list(edges, CjType.ArrayOfEdges, sort, (cjEdgeElement, cjWriter1) -> cjEdgeElement.fire(cjWriter1, sort));
        cjWriter.list(graphs, CjType.ArrayOfGraphs, sort, (cjGraphElement, cjWriter1) -> cjGraphElement.fire(cjWriter1, sort));

        cjWriter.graphEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        //noinspection RedundantCast
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Override
    public int indexOf(ICjGraph subGraph) {
        //noinspection SuspiciousMethodCalls
        return graphs.indexOf(subGraph);
    }

    @Override
    public int indexOf(ICjNode node) {
        //noinspection SuspiciousMethodCalls
        return nodes.indexOf(node);
    }

    @Override
    public int indexOf(ICjEdge edge) {
        //noinspection SuspiciousMethodCalls
        return edges.indexOf(edge);
    }

    @Override
    public Stream<ICjNode> nodes() {
        //noinspection RedundantCast
        return nodes.stream().map(x -> (ICjNode) x);
    }

    @Override
    public @NonNull ICjElement parent() {
        return Objects.requireNonNull(parent);
    }

    @Override
    public void removeEdge(ICjEdge edge) {
        //noinspection SuspiciousMethodCalls
        edges.remove(edge);
    }

    @Override
    public boolean removeGraph(ICjGraph graph) {
        //noinspection SuspiciousMethodCalls
        return graphs.remove(graph);
    }

    @Override
    public void removeNode(ICjNode node) {
        //noinspection SuspiciousMethodCalls
        nodes.remove(node);
    }

    public void removeNode(CjNodeElement node) {
        nodes.remove(node);
    }

}
