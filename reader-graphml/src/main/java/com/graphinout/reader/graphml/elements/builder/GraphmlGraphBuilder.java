package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlGraph;
import com.graphinout.reader.graphml.elements.IGraphmlLocator;
import com.graphinout.reader.graphml.elements.impl.GraphmlGraph;

import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

@SuppressWarnings("UnusedReturnValue")
public class GraphmlGraphBuilder extends GraphmlElementWithDescAndIdBuilder<GraphmlGraphBuilder> implements ILocatorBuilder {

    /** @Nullable */
    private IGraphmlGraph.EdgeDefault edgedefault;
    /** @Nullable */
    private IGraphmlLocator locator;

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
