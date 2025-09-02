package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDesc;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDescAndId;
import com.calpano.graphinout.base.graphml.IXmlElement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Most {@link IGraphmlElementWithDesc} impls also implement {@link IXmlElement}, but {@link IGraphmlDocument} does
 * not.
 */
public abstract class GraphmlElementWithDescAndId extends GraphmlElementWithId implements IGraphmlElementWithDescAndId {

    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected final IGraphmlDescription desc;

    public GraphmlElementWithDescAndId(@Nullable Map<String, String> attributes, @Nullable String id, @Nullable IGraphmlDescription desc) {
        super(attributes, id);
        this.desc = desc;
    }

    @Override
    public IGraphmlDescription desc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlElementWithDescAndId that = (IGraphmlElementWithDescAndId) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), desc);
    }

    @Override
    public String toString() {
        return "GraphmlElement{" + "id=" + id() + "desc=" + desc + ", custom=" + customXmlAttributes() + '}';
    }


}
