package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GioNode extends GioExtensibleElement {

    @JsonProperty("id")
    private Object id;

    @JsonProperty("label")
    private Object label;

    @JsonProperty("ports")
    private Object ports;

    public Object getId() {
        return id;
    }

    public Object getLabel() {
        return label;
    }

    public Object getPorts() {
        return ports;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public void setPorts(Object ports) {
        this.ports = ports;
    }

}
