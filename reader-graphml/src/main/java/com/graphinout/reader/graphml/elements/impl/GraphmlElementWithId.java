package com.graphinout.reader.graphml.elements.impl;

import com.graphinout.reader.graphml.elements.IGraphmlElementWithId;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public abstract class GraphmlElementWithId extends GraphmlElement implements IGraphmlElementWithId {

    protected final @Nullable String id;

    public GraphmlElementWithId(Map<String, String> attributes, @Nullable String id) {
        super(attributes);
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlElementWithId that = (IGraphmlElementWithId) o;
        return IGraphmlElementWithId.isEqual(this, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public @Nullable String id() {
        return id;
    }

    @Override
    public String toString() {
        return "GraphmlElementWithId{id='" + id + "' custom='" + customXmlAttributes() + "'}";
    }


}
