package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CjGraph extends CjExtensibleElement {

    @JsonProperty("nodes")
    private final List<CjNode> nodes = new ArrayList<>();
    @JsonProperty("edges")
    private final List<CjEdge> edges = new ArrayList<>();
    @JsonProperty("id")
    private Object id;
    @JsonProperty("directed")
    private Boolean directed;

    @JsonProperty("label")
    private Object label;

    public Boolean getDirected() {
        return directed;
    }

    public List<CjEdge> getEdges() {
        return edges;
    }

    public Object getId() {
        return id;
    }

    public Object getLabel() {
        return label;
    }

    public List<CjNode> getNodes() {
        return nodes;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }

    public void setEdges(List<CjEdge> edges) {
        this.edges.clear();
        this.edges.addAll(edges);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public void setNodes(List<CjNode> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

}
