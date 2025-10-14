package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.HashMap;

public class GraphmlDataBuilder extends GraphmlElementWithIdBuilder<GraphmlDataBuilder> {

    private @Nullable XmlFragmentString xmlValue;
    private String key;

    public GraphmlData build() {
        return new GraphmlData(id, attributes==null?new HashMap<>():attributes, xmlValue, key);
    }

    public GraphmlDataBuilder key(String key) {
        this.key = key;
        return this;
    }

    public String key() {
        return key;
    }

    /**
     * @param xmlValue content must have the correct xmlSpace as the effective XmlSpace as defined by surrounding graph
     *                 and maybe this element
     */
    public GraphmlDataBuilder xmlValue(@Nullable XmlFragmentString xmlValue) {
        if (xmlValue != null) {
            this.xmlValue = xmlValue;
        }
        return this;
    }

}
