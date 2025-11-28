package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.impl.GraphmlDefault;
import com.graphinout.foundation.xml.XmlFragmentString;

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
