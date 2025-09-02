package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlHyperEdge extends IGraphmlElementWithDescAndId {

    static GraphmlHyperEdgeBuilder builder() {
        return new GraphmlHyperEdgeBuilder();
    }

    List<IGraphmlEndpoint> endpoints();

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
    }

    @Override
    default String tagName() {
        return GraphmlElements.HYPER_EDGE;
    }

}
