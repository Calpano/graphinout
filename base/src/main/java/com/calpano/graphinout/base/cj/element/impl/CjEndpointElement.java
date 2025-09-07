package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjEndpointProperties;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class CjEndpointElement extends CjWithDataElement implements ICjEndpointProperties, ICjElement {

    private String node;
    private @Nullable String port;
    private @Nullable CjDirection direction;
    private @Nullable String type;
    private @Nullable String typeUri;
    private @Nullable String typeNode;

    CjEndpointElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.Endpoint;
    }

    @Nullable
    @Override
    public CjDirection direction() {
        return direction;
    }

    public CjEndpointElement direction(CjDirection direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void fire(CjWriter cjWriter) {
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

    public CjEndpointElement node(String node) {
        this.node = node;
        return this;
    }

    public CjEndpointElement port(@Nullable String port) {
        this.port = port;
        return this;
    }

    @Nullable
    @Override
    public String port() {
        return port;
    }

    public void type(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    @Override
    public String type() {
        return type;
    }

    public void typeNode(@Nullable String typeNode) {
        this.typeNode = typeNode;
    }

    @Nullable
    @Override
    public String typeNode() {
        return typeNode;
    }

    public void typeUri(@Nullable String typeUri) {
        this.typeUri = typeUri;
    }

    @Nullable
    @Override
    public String typeUri() {
        return typeUri;
    }

}
