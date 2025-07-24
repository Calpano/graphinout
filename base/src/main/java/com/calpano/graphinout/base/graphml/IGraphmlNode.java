package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlNode;

import java.util.Set;

public interface IGraphmlNode extends IGraphmlWithDescElement {

    static GraphmlNode.GraphmlNodeBuilder builder() {
        return new GraphmlNode.GraphmlNodeBuilder();
    }

    String id();
    IGraphmlLocator locator();

    default Set<String> builtInAttributes() {
        return Set.of(ATTRIBUTE_ID);
    }

    @Override
    default String tagName() {
        return "node";
    }
}
