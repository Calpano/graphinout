package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.impl.GraphmlLocator;

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
