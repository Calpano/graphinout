package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlData extends IGraphmlElementWithId {

    String ATTRIBUTE_KEY = "key";

    static GraphmlDataBuilder builder() {
        return new GraphmlDataBuilder();
    }

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_KEY, this::key);
    }

    boolean isRawXml();

    /** points to a {@link IGraphmlKey#id()} */
    String key();

    @Override
    default String tagName() {
        return GraphmlElements.DATA;
    }

    String value();

}
