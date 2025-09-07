package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CjPort extends CjExtensibleElement {

    @JsonProperty("ports")
    private final List<CjPort> ports = new ArrayList<>();
    @JsonProperty("id")
    private Object id;

    public Object getId() {
        return id;
    }

    public List<CjPort> getPorts() {
        return ports;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setPorts(List<CjPort> ports) {
        this.ports.clear();
        this.ports.addAll(ports);
    }

}
