package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlPort;
import com.graphinout.reader.graphml.elements.impl.GraphmlPort;

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
