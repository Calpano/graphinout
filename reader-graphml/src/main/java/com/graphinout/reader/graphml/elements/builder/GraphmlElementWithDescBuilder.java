package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlDescription;

/**
 * Has desc & attributes
 *
 * @param <T>
 */
public abstract class GraphmlElementWithDescBuilder<T extends GraphmlElementWithDescBuilder<T>> extends GraphmlElementBuilder<T> {

    protected IGraphmlDescription desc;

    @SuppressWarnings("unchecked")
    public T desc(IGraphmlDescription desc) {
        this.desc = desc;
        return (T) this;
    }

}
