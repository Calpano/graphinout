package com.calpano.graphinout.base.graphml;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A read-only GraphML element, has custom attributes.
 */
public interface IGraphmlElement extends IXmlElement {

    static boolean isEqual(IGraphmlElement a, IGraphmlElement b) {
        return Objects.equals(a.tagName(), b.tagName()) && Objects.equals(a.allAttributesNormalized(), b.allAttributesNormalized());
    }

    default Map<String, String> allAttributesNormalized() {
        Map<String, String> xmlAttributes = xmlAttributes();
        Map<String, String> map = xmlAttributes == null ? new HashMap<>() : new HashMap<>(xmlAttributes);
        graphmlAttributes((n, vs) -> {
            String value = vs.get();
            if (value != null) {
                map.put(n, value);
            }
        });
        return map;
    }

    /**
     * All attributes, except the built-in ones. "Users can add attributes to all GraphML elements." User defined extra
     * attributes, see
     * <a href="http://graphml.graphdrawing.org/specification.html">here</a>, bottom of page
     */
    default Map<String, String> customXmlAttributes() {
        Map<String, String> attributes = new HashMap<>(xmlAttributes());
        forEachGraphmlAttributeName(attributes::remove);
        return attributes;
    }

    default void forEachGraphmlAttributeName(Consumer<String> name) {
        graphmlAttributes((n, vs) -> name.accept(n));
    }

    /**
     * Send elements default attributes to the consumer.
     *
     * @param name_value (name, Supplier(@Nullable value))
     */
    void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value);

    default Map<String, String> graphmlAttributes() {
        Map<String, String> map = new HashMap<>();
        graphmlAttributes((n, vs) -> map.put(n, vs.get()));
        return map;
    }

}
