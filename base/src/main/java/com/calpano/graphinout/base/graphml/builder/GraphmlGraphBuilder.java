package com.calpano.graphinout.base.graphml.builder;

import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;

import javax.annotation.Nullable;

@SuppressWarnings("UnusedReturnValue")
public class GraphmlGraphBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlGraphBuilder> implements ILocatorBuilder {

    private @Nullable IGraphmlGraph.EdgeDefault edgedefault;
    private @Nullable IGraphmlLocator locator;

    @Override
    public IGraphmlGraph build() {
        return new GraphmlGraph(id, edgedefault, locator, attributes, desc);
    }

    public GraphmlGraphBuilder edgeDefault(@Nullable GraphmlGraph.EdgeDefault edgedefault) {
        this.edgedefault = edgedefault;
        return this;
    }

    public boolean isDirectedEdges() {
        return edgedefault == IGraphmlGraph.EdgeDefault.directed;
    }

    public @Nullable IGraphmlLocator locator() {
        return locator;
    }

    @Override
    public GraphmlGraphBuilder locator(IGraphmlLocator locator) {
        this.locator = locator;
        return this;
    }

}
