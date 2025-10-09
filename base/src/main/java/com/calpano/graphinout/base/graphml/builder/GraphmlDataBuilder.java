package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlData;

import javax.annotation.Nullable;

public class GraphmlDataBuilder extends GraphmlElementWithIdBuilder<GraphmlDataBuilder> {

    private @Nullable String value;
    private boolean isRawXml;
    private String key;

    public IGraphmlData build() {
        return new GraphmlData(id, attributes, value, isRawXml, key);
    }

    public GraphmlDataBuilder rawXml(boolean isRawXml) {
        this.isRawXml = isRawXml;
        return this;
    }

    public GraphmlDataBuilder key(String key) {
        this.key = key;
        return this;
    }

    public String key() {
        return key;
    }

    public GraphmlDataBuilder value( @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            this.value = value;
        }
        return this;
    }

}
