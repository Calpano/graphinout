package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlData extends IGraphmlElementWithId {

    String ATTRIBUTE_KEY = "key";

    static GraphmlDataBuilder builder() {
        return new GraphmlDataBuilder();
    }

    /**
     * @param keyId    the id of a {@code <key> element}
     * @param xmlValue content must have the correct xmlSpace as the effective XmlSpace as defined by surrounding graph
     *                 and maybe this element
     */
    static IGraphmlData of(String keyId, XmlFragmentString xmlValue) {
        return builder().key(keyId).xmlValue(xmlValue).build();
    }

    /**
     * The special Graphml XML attributes of this {@code <data>} element are 'id' (irrelevant for semantics) and 'key'
     * (for lookup in {@code <key>}.
     *
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_KEY, this::key);
    }

    /** points to a {@link IGraphmlKey#id()} */
    String key();

    @Override
    default String tagName() {
        return GraphmlElements.DATA;
    }

    /**
     * Does not take XML attributes into account.
     */
    @Nullable
    XmlFragmentString xmlValue();

}
