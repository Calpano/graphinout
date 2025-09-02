package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDefaultBuilder;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlDefault extends IGraphmlElement {

    static GraphmlDefaultBuilder builder() {
        return new GraphmlDefaultBuilder();
    }

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
    }

    @Override
    default String tagName() {
        return GraphmlElements.DEFAULT;
    }

    String value();


}
