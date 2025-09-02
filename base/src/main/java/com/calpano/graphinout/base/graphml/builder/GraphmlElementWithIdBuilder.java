package com.calpano.graphinout.base.graphml.builder;

/**
 * Has attributes and id
 */
public abstract class GraphmlElementWithIdBuilder<T extends GraphmlElementBuilder<T>> extends GraphmlElementBuilder<T> {

    protected String id;

    @SuppressWarnings("unchecked")
    public T id(String id) {
        this.id = id;
        return (T) this;
    }

}
