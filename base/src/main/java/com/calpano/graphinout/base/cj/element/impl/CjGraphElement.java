package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMeta;
import com.calpano.graphinout.base.cj.element.ICjGraphMetaMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjNodeMutable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjGraphElement extends CjHasDataAndLabelElement implements ICjGraphMutable {

    private final List<CjGraphElement> graphs = new ArrayList<>();
    private final List<CjNodeElement> nodes = new ArrayList<>();
    private final List<CjEdgeElement> edges = new ArrayList<>();
    private String id;
    private @Nullable CjGraphMetaElement meta;

    CjGraphElement(@Nullable CjHasDataElement parent) {
        super(parent);
    }

    @Override
    public void addEdge(Consumer<ICjEdgeMutable> edge) {
        CjEdgeElement edgeEvent = new CjEdgeElement(this);
        edge.accept(edgeEvent);
        edges.add(edgeEvent);
    }

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Override
    public void addNode(Consumer<ICjNodeMutable> node) {
        CjNodeElement n = new CjNodeElement(this);
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

    @Override
    public void id(String id) {
        this.id = id;
    }

    @Override
    public void meta(Consumer<ICjGraphMetaMutable> meta) {
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

}
