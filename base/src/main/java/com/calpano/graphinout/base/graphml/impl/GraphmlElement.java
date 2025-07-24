package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlElement;
import com.calpano.graphinout.base.graphml.IGraphmlWithDescElement;
import com.calpano.graphinout.base.graphml.IXmlElement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Most {@link IGraphmlWithDescElement} impls also implement {@link IXmlElement}, but {@link IGraphmlDocument} does not.
 */
public abstract class GraphmlElement implements IGraphmlWithDescElement {

    public static abstract class GraphmlElementBuilder {

        protected @Nullable Map<String, String> attributes;
        protected IGraphmlDescription desc;

        public abstract IGraphmlElement build();

        public GraphmlElementBuilder desc(IGraphmlDescription desc) {
            this.desc = desc;
            return this;
        }


        public GraphmlElementBuilder attributes(@Nullable Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

    }

    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected IGraphmlDescription desc;


    protected @Nullable Map<String, String> allAttributes;

    // Constructors
    public GraphmlElement() {
    }

    public GraphmlElement(@Nullable Map<String, String> allAttributes) {
        this.allAttributes = allAttributes;
    }

    public GraphmlElement(IGraphmlDescription desc) {
        this.desc = desc;
    }

    public GraphmlElement(@Nullable Map<String, String> allAttributes, IGraphmlDescription desc) {
        this.allAttributes = allAttributes;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlElement that = (GraphmlElement) o;
        return Objects.equals(allAttributes, that.allAttributes) && Objects.equals(desc, that.desc);
    }

    @Override
    public IGraphmlDescription desc() {
        return desc;
    }

    @Nullable
    @Override
    public Map<String, String> customXmlAttributes() {
        return allAttributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allAttributes, desc);
    }

    public void setDesc(GraphmlDescription desc) {
        this.desc = desc;
    }

    public void setAllAttributes(@Nullable Map<String, String> allAttributes) {
        this.allAttributes = allAttributes;
    }

    @Override
    public String toString() {
        return "GraphmlElement{" + "desc=" + desc + ", extraAttrib=" + allAttributes + '}';
    }


}
