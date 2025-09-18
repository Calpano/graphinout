package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.cj.element.ICjWithMutableId;
import com.calpano.graphinout.base.cj.element.ICjWithMutablePorts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjPortElement extends CjWithDataAndLabelElement implements ICjPort, ICjWithMutableId, ICjWithMutablePorts {

    private final List<CjPortElement> ports = new ArrayList<>();

    private String id;

    CjPortElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    @Override
    public CjPortElement addPort(Consumer<CjPortElement> consumer) {
        CjPortElement portElement = new CjPortElement(this);
        consumer.accept(portElement);
        ports.add(portElement);
        return portElement;
    }

    @Override
    public CjType cjType() {
        return CjType.Port;
    }

    @Override
    public void fire(CjWriter cjWriter) {
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

    public List<CjPortElement> portsMutable() {
        return ports;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPorts(List<CjPortElement> ports) {
        this.ports.clear();
        this.ports.addAll(ports);
    }

}
