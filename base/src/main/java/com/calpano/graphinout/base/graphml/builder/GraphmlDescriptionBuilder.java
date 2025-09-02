package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;

public class GraphmlDescriptionBuilder extends GraphmlElementBuilder<GraphmlDescriptionBuilder> {

    private String value;

    public GraphmlDescription build() {
        return new GraphmlDescription(attributes, value);
    }

    public GraphmlDescriptionBuilder value(String value) {
        this.value = value;
        return this;
    }

}
