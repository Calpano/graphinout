package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CjEndpoint extends CjExtensibleElement {

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("node")
    private Object node;

    @JsonProperty("port")
    private Object port;

    public String getDirection() {
        return direction;
    }

    public Object getNode() {
        return node;
    }

    public Object getPort() {
        return port;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public void setPort(Object port) {
        this.port = port;
    }

}
