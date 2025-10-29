package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjNodeElement extends CjHasDataAndLabelElement implements ICjNodeMutable {

    private final List<CjPortElement> ports = new java.util.ArrayList<>();
    private final List<CjGraphElement> graphs = new java.util.ArrayList<>();
    private String id;

    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement();
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    public void addPort(Consumer<ICjPortMutable> port) {
        CjPortElement portElement = new CjPortElement();
        port.accept(portElement);
        // TODO validate resulting portElement
        ports.add(portElement);
    }

    @Override
    public CjType cjType() {
        return CjType.Node;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CjNodeElement that)) return false;

        return ports.equals(that.ports) && graphs.equals(that.graphs) && Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "CjNodeElement{" +
                "ports=" + ports +
                ", graphs=" + graphs +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);

        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);

        cjWriter.nodeEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Override
    public int hashCode() {
        int result = ports.hashCode();
        result = 31 * result + graphs.hashCode();
        result = 31 * result + Objects.hashCode(id);
        return result;
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
    public Stream<ICjPort> ports() {
        return ports.stream().map(x -> (ICjPort) x);
    }

    public void setId(String id) {
        this.id = id;
    }

}
