package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjNodeElement extends CjNodeChunk implements ICjNodeMutable {

    private final ICjGraph parent;
    private final List<CjGraphElement> graphs = new java.util.ArrayList<>();

    public CjNodeElement(ICjGraph parent) {this.parent = parent;}

    public ICjGraphMutable addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }

    @Override
    public CjType cjType() {
        return CjType.Node;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CjNodeElement that)) return false;
        return super.equals(that) && graphs.equals(that.graphs);
    }

    @Override
    public void fire(ICjWriter cjWriter, boolean sort) {
        fireStartChunk(cjWriter, sort);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, sort, (cjGraphElement, cjWriter1) -> cjGraphElement.fire(cjWriter1, sort));
        cjWriter.nodeEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        //noinspection RedundantCast
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        // nodes with same local id might get different URI due to parents baseUri
        result = 31 * result + uri().hashCode();
        result = 31 * result + graphs.hashCode();
        return result;
    }

    @Override
    public int indexOf(ICjGraph subGraph) {
        //noinspection SuspiciousMethodCalls
        return graphs.indexOf(subGraph);
    }

    @Override
    public @NonNull ICjGraph parent() {
        return parent;
    }

    @Override
    public boolean removeGraph(ICjGraph graph) {
        //noinspection SuspiciousMethodCalls
        return graphs.remove(graph);
    }

    @Override
    public String toString() {
        return "CjNodeElement{" + super.toString() + ", graphs=" + graphs + '}';
    }


}
