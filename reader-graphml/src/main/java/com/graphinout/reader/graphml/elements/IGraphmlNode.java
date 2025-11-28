package com.graphinout.reader.graphml.elements;

import com.graphinout.reader.graphml.elements.builder.GraphmlNodeBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlNode extends IGraphmlElementWithDescAndId {

    static GraphmlNodeBuilder builder() {
        return new GraphmlNodeBuilder();
    }

    static IGraphmlNode of(String id) {
        return builder().id(id).build();
    }

    /**
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
    }

    @Nullable
    IGraphmlLocator locator();

    @Override
    default String tagName() {
        return GraphmlElements.NODE;
    }

}
