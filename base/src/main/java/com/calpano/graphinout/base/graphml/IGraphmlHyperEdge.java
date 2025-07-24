package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public interface IGraphmlHyperEdge extends IGraphmlWithDescElement, IXmlElement {

    String TAGNAME = "hyperedge";

    static GraphmlHyperEdge.GraphmlHyperEdgeBuilder builder(String id) {
        return new GraphmlHyperEdge.GraphmlHyperEdgeBuilder(id);
    }

    @Override
    LinkedHashMap<String, String> attributes();

    @Override
    String tagName();

    @Override
    Set<String> builtInAttributeNames();

    List<IGraphmlEndpoint> endpoints();

    String id();

}
