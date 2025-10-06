package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlDescriptionBuilder;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlDescription extends IGraphmlElement {


    static GraphmlDescriptionBuilder builder() {
        return new GraphmlDescriptionBuilder();
    }

    /** Use {@link #builder()} to also set custom attributes */
    static IGraphmlDescription of(String value) {
        return builder().value(value).build();
    }

    default Set<String> graphmlAttributeNames() {
        return Set.of();
    }

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
    }

    @Override
    default String tagName() {
        return GraphmlElements.DESC;
    }

    String value();

    void writeXml(XmlWriter xmlWriter) throws IOException;

}
