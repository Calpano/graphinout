package com.calpano.graphinout.base.gio.dom;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GioDocument extends GioExtensibleElement {

    @JsonProperty("graphs")
    private final List<GioGraph> graphs = new ArrayList<>();

    @JsonProperty("nodes")
    private final List<GioNode> nodes = new ArrayList<>();

    @JsonProperty("edges")
    private final List<GioEdge> edges = new ArrayList<>();

    public List<GioGraph> getGraphs() {
        return graphs;
    }

    public List<GioNode> getNodes() {
        return nodes;
    }

    public List<GioEdge> getEdges() {
        return edges;
    }

    void setGraph(List<GioGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setGraphs(List<GioGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setNodes(List<GioNode> nodes) {
        this.nodes.addAll(nodes);
    }

    void setEdges(List<GioEdge> edges) {
        this.edges.addAll(edges);
    }


}
