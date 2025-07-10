package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GioEndpoint extends GioExtensibleElement {

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("node")
    private Object node;

    @JsonProperty("port")
    private Object port;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public Object getPort() {
        return port;
    }

    public void setPort(Object port) {
        this.port = port;
    }
}
