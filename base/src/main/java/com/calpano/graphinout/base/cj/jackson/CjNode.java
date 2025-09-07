package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CjNode extends CjExtensibleElement {

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
