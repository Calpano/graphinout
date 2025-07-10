package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GioEdge extends GioExtensibleElement {

    @JsonProperty("endpoints")
    private final List<GioEndpoint> endpoints = new ArrayList<>();
    @JsonProperty("id")
    private Object id;
    @JsonProperty("source")
    private Object source;
    @JsonProperty("target")
    private Object target;
    @JsonProperty("sourcePort")
    private Object sourcePort;
    @JsonProperty("targetPort")
    private Object targetPort;
    @JsonProperty("label")
    private Object label;

    @JsonProperty("directed")
    private Boolean directed;

    public Boolean getDirected() {
        return directed;
    }

    public List<GioEndpoint> getEndpoints() {
        return endpoints;
    }

    public Object getId() {
        return id;
    }

    public Object getLabel() {
        return label;
    }

    public Object getSource() {
        return source;
    }

    public Object getSourcePort() {
        return sourcePort;
    }

    public Object getTarget() {
        return target;
    }

    public Object getTargetPort() {
        return targetPort;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }

    public void setEndpoints(List<GioEndpoint> endpoints) {
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setSourcePort(Object sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTargetPort(Object targetPort) {
        this.targetPort = targetPort;
    }

}
