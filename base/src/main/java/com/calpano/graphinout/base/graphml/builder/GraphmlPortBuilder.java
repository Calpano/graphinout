package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.impl.GraphmlPort;

public class GraphmlPortBuilder extends GraphmlElementWithDescBuilder<GraphmlPortBuilder> {

    private String name;

    @Override
    public IGraphmlPort build() {
        return new GraphmlPort(attributes, desc, name);
    }

    public GraphmlPortBuilder name(String name) {
        this.name = name;
        return this;
    }

}
