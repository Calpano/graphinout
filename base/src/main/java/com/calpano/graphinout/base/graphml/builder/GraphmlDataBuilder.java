package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlData;

public class GraphmlDataBuilder extends GraphmlElementWithIdBuilder<GraphmlDataBuilder> {

    private String value;
    private boolean containsRawXml;
    private String key;

    public IGraphmlData build() {
        return new GraphmlData(id, attributes, value, containsRawXml, key);
    }

    public void containsRawXml(boolean containsRawXml) {
        this.containsRawXml = containsRawXml;
    }

    public GraphmlDataBuilder key(String key) {
        this.key = key;
        return this;
    }

    public String key() {
        return key;
    }

    public GraphmlDataBuilder value(String value) {
        if (value != null && !value.isEmpty()) {
            this.value = value;
        }
        return this;
    }

}
