package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GioPort extends GioExtensibleElement {

    @JsonProperty("id")
    private Object id;

    @JsonProperty("ports")
    private final List<GioPort> ports = new ArrayList<>();

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public List<GioPort> getPorts() {
        return ports;
    }

    public void setPorts(List<GioPort> ports) {
        this.ports.clear();
        this.ports.addAll(ports);
    }
}
