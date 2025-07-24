package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;

import java.util.LinkedHashMap;
import java.util.Set;

public interface IGraphmlGraph extends IGraphmlWithDescElement, IXmlElement {

    String TAGNAME = "graph";
    String ATTRIBUTE_EDGEDEFAULT = "edgedefault";

    // Builder
    static GraphmlGraph.GraphmlGraphBuilder builder() {
        return new GraphmlGraph.GraphmlGraphBuilder();
    }

    @Override
    LinkedHashMap<String, String> attributes();

    @Override
    String tagName();

    @Override
    Set<String> builtInAttributeNames();

    String getId();

    IGraphmlLocator getLocator();

    boolean isDirectedEdges();

}
