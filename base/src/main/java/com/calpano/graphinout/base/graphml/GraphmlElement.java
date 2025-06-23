package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class GraphmlElement {

    /**
     * "Users can add attributes to all GraphML elements.". User defined extra attributes, see
     * http://graphml.graphdrawing.org/specification.html, bottom of page
     */
    protected @Nullable Map<String, String> extraAttrib;

    // Constructors
    public GraphmlElement() {
    }

    public GraphmlElement(@Nullable Map<String, String> extraAttrib) {
        this.extraAttrib = extraAttrib;
    }

    // Builder
    public static GraphmlElementBuilder builder() {
        return new GraphmlElementBuilder();
    }

    public static class GraphmlElementBuilder {
        protected @Nullable Map<String, String> extraAttrib;

        public GraphmlElementBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            this.extraAttrib = extraAttrib;
            return this;
        }

        public GraphmlElement build() {
            return new GraphmlElement(extraAttrib);
        }
    }

    // Getters and Setters
    public @Nullable Map<String, String> getExtraAttrib() {
        return extraAttrib;
    }

    public void setExtraAttrib(@Nullable Map<String, String> extraAttrib) {
        this.extraAttrib = extraAttrib;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlElement that = (GraphmlElement) o;
        return Objects.equals(extraAttrib, that.extraAttrib);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extraAttrib);
    }

    @Override
    public String toString() {
        return "GraphmlElement{" +
               "extraAttrib=" + extraAttrib +
               '}';
    }
}
