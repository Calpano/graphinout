package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.IGraphmlElement;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Has attributes
 *
 * @param <T>
 */
public abstract class GraphmlElementBuilder<T extends GraphmlElementBuilder<T>> {

    protected @Nullable Map<String, String> attributes;

    public T attribute(String attName, String attValue) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(attName, attValue);
        //noinspection unchecked
        return (T) this;
    }

    public T attributes(@Nullable Map<String, String> attributes) {
        this.attributes = attributes;
        //noinspection unchecked
        return (T) this;
    }

    public abstract IGraphmlElement build();

}
