package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlElement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public abstract class GraphmlElement implements IGraphmlElement {

    protected final @Nullable Map<String, String> xmlAttributes;

    /**
     * @param xmlAttributes <em>All</em> attributes, including those interpreted by an element.
     */
    public GraphmlElement(@Nullable Map<String, String> xmlAttributes) {
        this.xmlAttributes = xmlAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlElement that = (IGraphmlElement) o;
        return Objects.equals(allAttributesNormalized(), that.allAttributesNormalized());
    }

    @Override
    public int hashCode() {
        return Objects.hash(xmlAttributes);
    }

    @Override
    public String toString() {
        return "GraphmlElement{" + "custom=" + customXmlAttributes() + '}';
    }

    @Override
    @Nullable
    public Map<String, String> xmlAttributes() {
        return xmlAttributes;
    }


}
