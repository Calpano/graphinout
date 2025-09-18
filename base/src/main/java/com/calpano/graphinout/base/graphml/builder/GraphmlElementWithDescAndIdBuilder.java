package com.calpano.graphinout.base.graphml.builder;

/**
 * Has desc, id & attributes
 *
 * @param <T>
 */
public abstract class GraphmlElementWithDescAndIdBuilder<T extends GraphmlElementWithDescAndIdBuilder<T>> extends GraphmlElementWithDescBuilder<T> implements IIdBuilder {

    protected String id;

    @Override
    @SuppressWarnings("unchecked")
    public T id(String id) {
        this.id = id;
        return (T) this;
    }

}
