package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDefaultBuilder;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlDefault extends IGraphmlElement {

    static GraphmlDefaultBuilder builder() {
        return new GraphmlDefaultBuilder();
    }

    /** Use {@link #builder()} to also set custom attributes */
    static IGraphmlDefault of(XmlFragmentString xmlValue) {
        return builder().value(xmlValue).build();
    }

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
    }

    @Override
    default String tagName() {
        return GraphmlElements.DEFAULT;
    }

    XmlFragmentString xmlValue();


}
