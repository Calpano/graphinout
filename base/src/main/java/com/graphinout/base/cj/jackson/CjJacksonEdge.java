package com.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CjJacksonEdge extends CjJacksonExtensibleElement {

    @JsonProperty("endpoints")
    private final List<CjJacksonEndpoint> endpoints = new ArrayList<>();
    @JsonProperty("id")
    private String id;
    @JsonProperty("source")
    private String source;
    @JsonProperty("target")
    private String target;
    @JsonProperty("sourcePort")
    private String sourcePort;
    @JsonProperty("targetPort")
    private String targetPort;
    @JsonProperty("label")
    private String label;

    @JsonProperty("directed")
    private Boolean directed;

    public Boolean getDirected() {
        return directed;
    }

    public List<CjJacksonEndpoint> getEndpoints() {
        return endpoints;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getSource() {
        return source;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public String getTarget() {
        return target;
    }

    public Object getTargetPort() {
        return targetPort;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }

    public void setEndpoints(List<CjJacksonEndpoint> endpoints) {
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

}
