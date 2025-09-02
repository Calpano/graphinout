package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.impl.GraphmlDefault;

public class GraphmlDefaultBuilder extends GraphmlElementBuilder<GraphmlDefaultBuilder> {

    private String value;

    public IGraphmlDefault build() {
        return new GraphmlDefault(attributes, value);
    }

    public GraphmlDefaultBuilder value(String value) {
        this.value = value;
        return this;
    }

}
