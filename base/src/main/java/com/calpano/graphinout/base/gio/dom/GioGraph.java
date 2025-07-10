package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GioGraph extends GioExtensibleElement {

    @JsonProperty("id")
    private Object id;

    @JsonProperty("nodes")
    private final List<GioNode> nodes = new ArrayList<>();

    @JsonProperty("edges")
    private final List<GioEdge> edges = new ArrayList<>();

    @JsonProperty("directed")
    private Boolean directed;

    @JsonProperty("label")
    private Object label;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public List<GioNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GioNode> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    public List<GioEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GioEdge> edges) {
        this.edges.clear();
        this.edges.addAll(edges);
    }

    public Boolean getDirected() {
        return directed;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object label) {
        this.label = label;
    }
}
