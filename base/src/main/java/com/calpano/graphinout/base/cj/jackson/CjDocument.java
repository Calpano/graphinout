package com.calpano.graphinout.base.cj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A CJ document
 */
public class CjDocument extends CjExtensibleElement {

    @JsonProperty("graphs")
    private final List<CjGraph> graphs = new ArrayList<>();

    @JsonProperty("nodes")
    private final List<CjNode> nodes = new ArrayList<>();

    @JsonProperty("edges")
    private final List<CjEdge> edges = new ArrayList<>();

    public List<CjEdge> getEdges() {
        return edges;
    }

    public List<CjGraph> getGraphs() {
        return graphs;
    }

    public List<CjNode> getNodes() {
        return nodes;
    }

    void setEdges(List<CjEdge> edges) {
        this.edges.addAll(edges);
    }

    void setGraph(List<CjGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setGraphs(List<CjGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setNodes(List<CjNode> nodes) {
        this.nodes.addAll(nodes);
    }


}
