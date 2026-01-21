package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.ICjPortMutable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CjNodeChunk extends CjHasDataAndLabelElement implements ICjNodeChunkMutable {

    private final List<CjPortElement> ports = new java.util.ArrayList<>();
    private final List<ICjElementType> types = new java.util.ArrayList<>();
    private String id;

    public void addPort(Consumer<ICjPortMutable> port) {
        CjPortElement portElement = new CjPortElement();
        port.accept(portElement);
        // TODO validate resulting portElement
        ports.add(portElement);
    }

    @Override
    public void addType(ICjElementType type) {
        types.add(type);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CjNodeChunk that)) return false;
        return ports.equals(that.ports) && types.equals(that.types) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = ports.hashCode();
        result = 31 * result + types.hashCode();
        result = 31 * result + Objects.hashCode(id);
        return result;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CjNodeChunk id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Stream<ICjPort> ports() {
        return ports.stream().map(x -> (ICjPort) x);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CjNodeElement{" + "ports=" + ports + ", types=" + types + ", id='" + id + '\'' + '}';
    }

    @Override
    public Stream<ICjElementType> types() {
        return types.stream();
    }

}
