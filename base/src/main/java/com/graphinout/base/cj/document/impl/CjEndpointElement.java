package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import org.jspecify.annotations.Nullable;

import static java.util.Optional.ofNullable;

public class CjEndpointElement extends CjHasDataElement implements ICjEndpointMutable {

    private String node;
    private @Nullable String port;
    private @Nullable CjDirection direction;
    private @Nullable String type;

    @Override
    public CjType cjType() {
        return CjType.Endpoint;
    }

    @Override
    public CjDirection direction() {
        return direction == null ? CjDirection.UNDIR : direction;
    }

    @Override
    public ICjEndpointMutable direction(CjDirection direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void fire(ICjWriter cjWriter, boolean sort) {
        cjWriter.endpointStart();
        cjWriter.nodeId(node);
        ofNullable(port).ifPresent(cjWriter::portId);
        ofNullable(direction).ifPresent(cjWriter::direction);
        // CJ 7.0.0: endpoints can have a type
        ofNullable(type).ifPresent(t -> cjWriter.edgeType(ICjElementType.of(t)));
        fireDataMaybe(cjWriter);
        cjWriter.endpointEnd();
    }

    @Override
    public String node() {
        return node;
    }

    @Override
    public ICjEndpointMutable node(String node) {
        this.node = node;
        return this;
    }

    @Override
    public ICjEndpointMutable port(@Nullable String port) {
        this.port = port;
        return this;
    }

    @Nullable
    @Override
    public String port() {
        return port;
    }

    @Override
    public ICjEndpointMutable type(@Nullable String type) {
        this.type = type;
        return this;
    }

    @Nullable
    @Override
    public String type() {
        return type;
    }

}
