package com.graphinout.base.cj.helper.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A CJ document
 */
public class CjJacksonDocument extends CjJacksonExtensibleElement {

    @JsonProperty("graphs")
    private final List<CjJacksonGraph> graphs = new ArrayList<>();

    @JsonProperty("nodes")
    private final List<CjJacksonNode> nodes = new ArrayList<>();

    @JsonProperty("edges")
    private final List<CjJacksonEdge> edges = new ArrayList<>();

    public List<CjJacksonEdge> getEdges() {
        return edges;
    }

    public List<CjJacksonGraph> getGraphs() {
        return graphs;
    }

    public List<CjJacksonNode> getNodes() {
        return nodes;
    }

    void setEdges(List<CjJacksonEdge> edges) {
        this.edges.addAll(edges);
    }

    void setGraph(List<CjJacksonGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setGraphs(List<CjJacksonGraph> graphs) {
        this.graphs.addAll(graphs);
    }

    void setNodes(List<CjJacksonNode> nodes) {
        this.nodes.addAll(nodes);
    }


}
