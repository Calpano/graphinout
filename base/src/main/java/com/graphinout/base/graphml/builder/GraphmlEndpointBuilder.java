package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.GraphmlDirection;
import com.graphinout.base.graphml.impl.GraphmlEndpoint;

public class GraphmlEndpointBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlEndpointBuilder> {

    private String node;
    private String port;
    private GraphmlDirection type = GraphmlDirection.Undirected;

    public GraphmlEndpoint build() {
        return new GraphmlEndpoint(attributes, id, node, port, type, desc);
    }

    public GraphmlEndpointBuilder node(String node) {
        this.node = node;
        return this;
    }

    public GraphmlEndpointBuilder port(String port) {
        this.port = port;
        return this;
    }

    public GraphmlEndpointBuilder type(GraphmlDirection type) {
        this.type = type;
        return this;
    }

}
