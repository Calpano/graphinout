package com.graphinout.reader.graphml.elements;

import com.graphinout.reader.graphml.elements.builder.GraphmlDescriptionBuilder;
import com.graphinout.foundation.xml.XmlFragmentString;
import com.graphinout.foundation.xml.XmlWriter;

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
        return of(XmlFragmentString.ofPlainText(value));
    }

    static IGraphmlDescription of(XmlFragmentString xmlValue) {
        return builder().xmlValue(xmlValue).build();
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

    void writeXml(XmlWriter xmlWriter) throws IOException;

    XmlFragmentString xmlValue();

}
