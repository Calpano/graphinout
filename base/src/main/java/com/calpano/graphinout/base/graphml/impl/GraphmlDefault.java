package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDefault;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and
 * to the whole collection of graphs described by the content of &lt;graphml&gt;.
 * <p>
 * These functions are declared by &lt;key&gt; elements (children of &lt;graphml&gt;) and defined by &lt;data&gt;
 * elements. The (optional) &lt;default&gt; child of &lt;key&gt; gives the default value for the corresponding function.
 * Occurence: &lt;key&gt;.
 */
public class GraphmlDefault implements IGraphmlDefault {

    public static class GraphmlDefaultBuilder {

        private String value;
        private String defaultType;

        public IGraphmlDefault build() {
            return new GraphmlDefault(value, defaultType);
        }

        /**
         * FIXME should be used
         */
        public GraphmlDefaultBuilder defaultType(String defaultType) {
            this.defaultType = defaultType;
            return this;
        }

        public GraphmlDefaultBuilder value(String value) {
            this.value = value;
            return this;
        }

    }

    /**
     * the default value for the corresponding function.
     */
    private final String value;
    private final String defaultType;

    public GraphmlDefault(String value, String defaultType) {
        this.value = value;
        this.defaultType = defaultType;
    }

    @Override
    public LinkedHashMap<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (defaultType != null) attributes.put("default.type", defaultType);

        return attributes;
    }


    @Override
    public String defaultType() {
        return defaultType;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlDefault that = (GraphmlDefault) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(defaultType, that.defaultType);
    }

    public String getDefaultType() {
        return defaultType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, defaultType);
    }

    @Override
    public String toString() {
        return "GraphmlDefault{" +
                "value='" + value + '\'' +
                ", defaultType='" + defaultType + '\'' +
                '}';
    }

    @Override
    public String value() {
        return value;
    }

}
