package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

// IMPROVE split into GraphmlElementWithData and GraphmlElementWithDesc
public class GraphmlGraphCommonElement extends GraphmlElement {
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected GraphmlDescription desc;

    // Constructors
    public GraphmlGraphCommonElement() {
        super();
    }

    public GraphmlGraphCommonElement(GraphmlDescription desc) {
        super();
        this.desc = desc;
    }

    public GraphmlGraphCommonElement(@Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib);
        this.desc = desc;
    }

    // Builder
    public static GraphmlGraphCommonElementBuilder builder() {
        return new GraphmlGraphCommonElementBuilder();
    }

    public static class GraphmlGraphCommonElementBuilder extends GraphmlElementBuilder {
        protected GraphmlDescription desc;

        public GraphmlGraphCommonElementBuilder desc(GraphmlDescription desc) {
            this.desc = desc;
            return this;
        }

        @Override
        public GraphmlGraphCommonElementBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        @Override
        public GraphmlGraphCommonElement build() {
            return new GraphmlGraphCommonElement(extraAttrib, desc);
        }
    }

    // Getters and Setters
    public GraphmlDescription getDesc() {
        return desc;
    }

    public void setDesc(GraphmlDescription desc) {
        this.desc = desc;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlGraphCommonElement that = (GraphmlGraphCommonElement) o;
        return Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), desc);
    }

    @Override
    public String toString() {
        return "GraphmlGraphCommonElement{" +
               "desc=" + desc +
               ", extraAttrib=" + extraAttrib +
               '}';
    }
}
