package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlGraph;
import com.graphinout.reader.graphml.elements.IGraphmlLocator;
import com.graphinout.reader.graphml.elements.impl.GraphmlGraph;

import javax.annotation.Nullable;

import static com.graphinout.foundation.util.Nullables.nonNullOrDefault;

@SuppressWarnings("UnusedReturnValue")
public class GraphmlGraphBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlGraphBuilder> implements ILocatorBuilder {

    private @Nullable IGraphmlGraph.EdgeDefault edgedefault;
    private @Nullable IGraphmlLocator locator;

    @Override
    public IGraphmlGraph build() {
        return new GraphmlGraph(id, edgeDefault(), locator, attributes, desc);
    }

    public GraphmlGraphBuilder edgeDefault(GraphmlGraph.EdgeDefault edgedefault) {
        this.edgedefault = edgedefault;
        return this;
    }

    public IGraphmlGraph.EdgeDefault edgeDefault() {
        return nonNullOrDefault( edgedefault, IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT);
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
