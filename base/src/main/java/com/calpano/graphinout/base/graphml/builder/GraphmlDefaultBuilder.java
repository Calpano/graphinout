package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.impl.GraphmlDefault;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

public class GraphmlDefaultBuilder extends GraphmlElementBuilder<GraphmlDefaultBuilder> {

    private XmlFragmentString xmlValue;

    public GraphmlDefault build() {
        return new GraphmlDefault(attributes, xmlValue);
    }

    public GraphmlDefaultBuilder value(XmlFragmentString xmlValue) {
        this.xmlValue = xmlValue;
        return this;
    }

}
