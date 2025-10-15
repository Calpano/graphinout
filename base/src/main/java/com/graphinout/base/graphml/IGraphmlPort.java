package com.graphinout.base.graphml;

import com.graphinout.base.graphml.builder.GraphmlPortBuilder;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlPort extends IGraphmlElementWithDesc {

    String ATTRIBUTE_NAME = "name";

    static GraphmlPortBuilder builder() {
        return new GraphmlPortBuilder();
    }

    /**
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_NAME, this::name);
    }

    /** The name of the port, quite similar to an id */
    String name();

    @Override
    default String tagName() {
        return GraphmlElements.PORT;
    }

}
