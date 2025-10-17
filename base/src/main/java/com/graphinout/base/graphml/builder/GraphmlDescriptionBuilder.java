package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.impl.GraphmlDescription;
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
