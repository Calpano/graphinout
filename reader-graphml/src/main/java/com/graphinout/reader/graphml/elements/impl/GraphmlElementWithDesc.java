package com.graphinout.reader.graphml.elements.impl;

import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlDocument;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithDesc;
import com.graphinout.reader.graphml.elements.IXmlElement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Most {@link IGraphmlElementWithDesc} impls also implement {@link IXmlElement}, but {@link IGraphmlDocument} does
 * not.
 */
public abstract class GraphmlElementWithDesc extends GraphmlElement implements IGraphmlElementWithDesc {

    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected final @Nullable IGraphmlDescription desc;

    public GraphmlElementWithDesc(Map<String, String> attributes, @Nullable IGraphmlDescription desc) {
        super(attributes);
        this.desc = desc;
    }

    @Override
    @Nullable
    public IGraphmlDescription desc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlElementWithDesc that = (IGraphmlElementWithDesc) o;
        return IGraphmlElementWithDesc.isEqual(this, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), desc);
    }

    @Override
    public String toString() {
        return "GraphmlElementWithDesc{" + "desc=" + desc + ", custom=" + customXmlAttributes() + '}';
    }


}
