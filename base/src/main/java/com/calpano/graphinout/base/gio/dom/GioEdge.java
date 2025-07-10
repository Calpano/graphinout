package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GioEdge extends GioExtensibleElement {

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

    @JsonProperty("endpoints")
    private final List<GioEndpoint> endpoints = new ArrayList<>();

    @JsonProperty("label")
    private Object label;

    @JsonProperty("directed")
    private Boolean directed;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Object sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Object getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(Object targetPort) {
        this.targetPort = targetPort;
    }

    public List<GioEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<GioEndpoint> endpoints) {
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
    }

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public Boolean getDirected() {
        return directed;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }
}
