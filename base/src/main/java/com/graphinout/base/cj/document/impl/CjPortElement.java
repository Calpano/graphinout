package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjPortElement extends CjHasDataAndLabelElement implements ICjPortMutable {

    private final List<CjPortElement> ports = new ArrayList<>();

    private String id;

    @Override
    public void addPort(Consumer<ICjPortMutable> consumer) {
        CjPortElement portElement = new CjPortElement();
        consumer.accept(portElement);
        ports.add(portElement);
    }

    @Override
    public CjType cjType() {
        return CjType.Port;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        cjWriter.portStart();
        cjWriter.maybe(id, cjWriter::id);
        fireDataMaybe(cjWriter);
        fireLabelMaybe(cjWriter);
        cjWriter.list(ports, CjType.ArrayOfPorts, CjPortElement::fire);
        cjWriter.portEnd();
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

}
