package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlLocatorBuilder;

import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlLocator extends IGraphmlElement {

    String ATTRIBUTE_XMLNS_XLINK = "xmlns:xlink";
    String ATTRIBUTE_XLINK_HREF = "xlink:href";
    String ATTRIBUTE_XLINK_TYPE = "xlink:type";

    static GraphmlLocatorBuilder builder() {
        return new GraphmlLocatorBuilder();
    }

    /**
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_XLINK_HREF, () -> xlinkHref().toExternalForm());
        name_value.accept(ATTRIBUTE_XMLNS_XLINK, () -> "http://www.w3.org/TR/2000/PR-xlink-20001220/");
        name_value.accept(ATTRIBUTE_XLINK_TYPE, () -> "simple");
    }

    @Override
    default String tagName() {
        return GraphmlElements.LOCATOR;
    }

    URL xlinkHref();

}
