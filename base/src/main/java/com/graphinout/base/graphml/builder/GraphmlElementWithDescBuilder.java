package com.graphinout.base.graphml.builder;

import com.graphinout.base.graphml.IGraphmlDescription;

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
