package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlPort;

import java.util.LinkedHashMap;
import java.util.Set;

public interface IGraphmlPort extends IGraphmlWithDescElement {

    String TAGNAME = "port";
    String ATTRIBUTE_NAME = "name";

    static GraphmlPort.GraphmlPortBuilder builder() {
        return new GraphmlPort.GraphmlPortBuilder();
    }

    LinkedHashMap<String, String> attributes();

    Set<String> builtInAttributeNames();

    String name();

    String tagName();

}
