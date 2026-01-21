package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjEdgeElement extends CjEdgeChunk implements ICjEdgeMutable {

    private final ICjGraph parent;
    private final List<CjGraphElement> graphs = new ArrayList<>();

    public CjEdgeElement(ICjGraph parent) {this.parent = parent;}

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Override
    public CjType cjType() {
        return CjType.Edge;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, ICjGraph::fire);
        cjWriter.edgeEnd();
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }


    @Override
    public int indexOf(ICjGraph subGraph) {
        //noinspection SuspiciousMethodCalls
        return graphs.indexOf(subGraph);
    }

    @Override
    public @NonNull ICjElement parent() {
        return parent;
    }

}
