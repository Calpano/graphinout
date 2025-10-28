package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.impl.GraphmlDescription;
import com.graphinout.foundation.xml.XmlFragmentString;

public class GraphmlDescriptionBuilder extends GraphmlElementBuilder<GraphmlDescriptionBuilder> {

    private XmlFragmentString xmlValue;

    public GraphmlDescription build() {
        return new GraphmlDescription(attributes, xmlValue);
    }

    public GraphmlDescriptionBuilder xmlValue(XmlFragmentString value) {
        this.xmlValue = value;
        return this;
    }

}
