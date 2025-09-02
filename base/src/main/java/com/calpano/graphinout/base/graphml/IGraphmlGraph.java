package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlGraph extends IGraphmlElementWithDescAndId {

    /**
     * The edgedefault attribute defines the default value of the edge attribute directed. The default value for
     * directed is directed.
     */
    enum EdgeDefault {
        /** the default */
        directed, undirected
    }

    String ATTRIBUTE_EDGE_DEFAULT = "edgedefault";

    static GraphmlGraphBuilder builder() {
        return new GraphmlGraphBuilder();
    }

    EdgeDefault edgeDefault();

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_EDGE_DEFAULT, () -> edgeDefault().name());
    }

    default boolean isDirectedEdges() {
        // default is directed for null
        return edgeDefault() != EdgeDefault.undirected;
    }

    @Nullable
    IGraphmlLocator locator();

    @Override
    default String tagName() {
        return GraphmlElements.GRAPH;
    }

}
