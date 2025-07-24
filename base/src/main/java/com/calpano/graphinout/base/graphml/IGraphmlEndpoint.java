package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.base.graphml.impl.GraphmlEndpoint;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public interface IGraphmlEndpoint extends IXmlElement {

    String TAGNAME = "endpoint";
    String ATTRIBUTE_ID = "id";
    String ATTRIBUTE_NODE = "node";
    String ATTRIBUTE_PORT = "port";
    String ATTRIBUTE_TYPE = "type";

    // Builder
    static GraphmlEndpoint.GraphmlEndpointBuilder builder() {
        return new GraphmlEndpoint.GraphmlEndpointBuilder();
    }

    @Override
    LinkedHashMap<String, String> attributes();

    @Override
    String tagName();

    GraphmlDescription desc();

    String id();

    String node();

    @Nullable
    String port();

    /** GraphML edge type = direction */
    GraphmlDirection type();

}
