package com.calpano.graphinout.base.cj.helper.jackson;

import com.calpano.graphinout.base.cj.CjDirection;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CjJacksonEndpoint extends CjJacksonExtensibleElement {

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("node")
    private String node;

    @JsonProperty("port")
    private String port;

    public CjDirection cjDirection() {
        return CjDirection.of(direction);
    }

    public String getDirection() {
        return direction;
    }

    public String getNode() {
        return node;
    }

    public String getPort() {
        return port;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
