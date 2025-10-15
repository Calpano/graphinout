package com.graphinout.base.cj.element.impl;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.ICjEndpointMutable;
import com.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class CjEndpointElement extends CjHasDataElement implements ICjEndpointMutable {

    private String node;
    private @Nullable String port;
    private @Nullable CjDirection direction;
    private @Nullable String type;
    private @Nullable String typeUri;
    private @Nullable String typeNode;

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
    public void fire(ICjWriter cjWriter) {
        cjWriter.endpointStart();
        cjWriter.nodeId(node);
        ofNullable(port).ifPresent(cjWriter::portId);
        ofNullable(direction).ifPresent(cjWriter::direction);
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

    @Override
    public ICjEndpointMutable typeNode(@Nullable String typeNode) {
        this.typeNode = typeNode;
        return this;
    }

    @Nullable
    @Override
    public String typeNode() {
        return typeNode;
    }

    @Override
    public ICjEndpointMutable typeUri(@Nullable String typeUri) {
        this.typeUri = typeUri;
        return this;
    }

    @Nullable
    @Override
    public String typeUri() {
        return typeUri;
    }

}
