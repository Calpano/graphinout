package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlData extends IGraphmlElementWithId {

    String ATTRIBUTE_KEY = "key";

    static GraphmlDataBuilder builder() {
        return new GraphmlDataBuilder();
    }

    static IGraphmlData ofPlainString(String key, String value) {
        return builder().key(key).value(value).build();
    }

    static IGraphmlData ofRawXml(String key, String value) {
        return builder().key(key).value(value).rawXml(true).build();
    }

    /**
     * The special Graphml XML attributes of this {@code <data>} element are 'id' (irrelevant for semantics) and 'key' (for lookup in {@code <key>}.
     * @param name_value (name, Supplier(@Nullable value))
     */
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
