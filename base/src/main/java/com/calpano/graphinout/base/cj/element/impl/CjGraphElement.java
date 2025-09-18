package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMeta;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjWithMutableGraphs;
import com.calpano.graphinout.base.cj.element.ICjWithMutableId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjGraphElement extends CjWithDataAndLabelElement implements ICjGraph, ICjWithMutableId, ICjWithMutableGraphs {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private final List<CjNodeElement> nodes = new ArrayList<>();
    private final List<CjEdgeElement> edges = new ArrayList<>();
    private String id;
    private @Nullable CjGraphMetaElement meta;

    CjGraphElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    public CjEdgeElement addEdge(Consumer<CjEdgeElement> edge) {
        CjEdgeElement edgeEvent = new CjEdgeElement(this);
        edge.accept(edgeEvent);
        edges.add(edgeEvent);
        return edgeEvent;
    }

    public CjGraphElement addGraph(Consumer<CjGraphElement> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }

    public CjNodeElement addNode(Consumer<CjNodeElement> node) {
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
        return edges.stream().map(x -> (ICjEdge) x);
    }

    public List<CjEdgeElement> edgesMutable() {
        return edges;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.graphStart();
        cjWriter.maybe(id, cjWriter::id);
        cjWriter.maybe(meta, meta -> meta.fire(cjWriter));
        // TODO meta
        fireDataMaybe(cjWriter);
        fireLabelMaybe(cjWriter);

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

    public void id(String id) {
        this.id = id;
    }

    public void meta(Consumer<CjGraphMetaElement> meta) {
        this.meta = new CjGraphMetaElement(this);
        meta.accept(this.meta);
    }

    @Nullable
    @Override
    public ICjGraphMeta meta() {
        return meta;
    }

    @Override
    public Stream<ICjNode> nodes() {
        return nodes.stream().map(x -> (ICjNode) x);
    }

    public List<CjNodeElement> nodesMutable() {
        return nodes;
    }

}
