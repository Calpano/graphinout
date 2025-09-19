package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.cj.element.ICjPortMutable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjPortElement extends CjHasDataAndLabelElement implements ICjPortMutable {

    private final List<CjPortElement> ports = new ArrayList<>();

    private String id;

    CjPortElement(@Nullable CjHasDataElement parent) {
        super(parent);
    }

    @Override
    public void addPort(Consumer<ICjPortMutable> consumer) {
        CjPortElement portElement = new CjPortElement(this);
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
