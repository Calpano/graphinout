package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.element.ICjNodeMutable;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.cj.element.ICjPortMutable;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import java.util.List;
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
