package com.graphinout.reader.graphml.elements;

import com.graphinout.reader.graphml.elements.builder.GraphmlEndpointBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlEndpoint extends IGraphmlElementWithDescAndId {

    String ATTRIBUTE_NODE = "node";
    String ATTRIBUTE_PORT = "port";
    String ATTRIBUTE_TYPE = "type";

    static GraphmlEndpointBuilder builder() {
        return new GraphmlEndpointBuilder();
    }

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_NODE, this::node);
        name_value.accept(ATTRIBUTE_PORT, this::port);
        name_value.accept(ATTRIBUTE_TYPE, () -> type().xmlValue());
    }

    String node();

    @Nullable
    String port();

    @Override
    default String tagName() {
        return GraphmlElements.ENDPOINT;
    }

    /** GraphML edge type = direction */
    GraphmlDirection type();

}
