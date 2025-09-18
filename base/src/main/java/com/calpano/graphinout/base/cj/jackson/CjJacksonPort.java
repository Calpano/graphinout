package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CjJacksonPort extends CjJacksonExtensibleElement {

    @JsonProperty("ports")
    private final List<CjJacksonPort> ports = new ArrayList<>();
    @JsonProperty("id")
    private Object id;

    public Object getId() {
        return id;
    }

    public List<CjJacksonPort> getPorts() {
        return ports;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setPorts(List<CjJacksonPort> ports) {
        this.ports.clear();
        this.ports.addAll(ports);
    }

}
