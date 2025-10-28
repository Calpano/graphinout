package com.graphinout.reader.graphml.elements.builder;

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
