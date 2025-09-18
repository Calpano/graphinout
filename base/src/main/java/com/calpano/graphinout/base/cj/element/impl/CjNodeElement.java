package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.cj.element.ICjWithMutableGraphs;
import com.calpano.graphinout.base.cj.element.ICjWithMutableId;
import com.calpano.graphinout.base.cj.element.ICjWithMutablePorts;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjNodeElement extends CjWithDataAndLabelElement implements ICjNode, ICjWithMutableId, ICjWithMutableGraphs, ICjWithMutablePorts {

    private final List<CjPortElement> ports = new java.util.ArrayList<>();
    private final List<CjGraphElement> graphs = new java.util.ArrayList<>();
    private String id;

    CjNodeElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    public CjGraphElement addGraph(Consumer<CjGraphElement> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
        return graphElement;
    }


    public CjPortElement addPort(Consumer<CjPortElement> port) {
        CjPortElement portElement = new CjPortElement(this);
        port.accept(portElement);
        // TODO validate resulting portElement
        ports.add(portElement);
        return portElement;
    }

    @Override
    public CjType cjType() {
        return CjType.Node;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.nodeStart();
        // streaming order: id, label, data, ports, graphs
        cjWriter.maybe(id, cjWriter::id);
        fireLabelMaybe(cjWriter);
        cjWriter.list(ports, CjType.ArrayOfPorts, CjPortElement::fire);
        fireDataMaybe(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);

        cjWriter.nodeEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    public List<CjGraphElement> graphsMutable() {
        return graphs;
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
