package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlLocator;
import com.graphinout.reader.graphml.elements.impl.GraphmlLocator;

import java.net.URL;

public class GraphmlLocatorBuilder extends GraphmlElementBuilder<GraphmlLocatorBuilder> {

    private URL xLinkHref;

    public IGraphmlLocator build() {
        return new GraphmlLocator(attributes, xLinkHref);
    }

    public GraphmlLocatorBuilder xLinkHref(URL xLinkHref) {
        this.xLinkHref = xLinkHref;
        return this;
    }

}
