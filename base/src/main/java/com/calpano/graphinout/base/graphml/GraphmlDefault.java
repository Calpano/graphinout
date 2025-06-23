package com.calpano.graphinout.base.graphml;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author rbaba

 * @implNote In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to the
 * whole collection of graphs described by the content of <graphml>.
 * <p>
 * These functions are declared by <key> elements (children of <graphml>) and defined by <data> elements.
 * The (optional) <default> child of <key> gives the default value for the corresponding function. Occurence: <key>.
 */
public class GraphmlDefault implements XMLValue {
    public static final String TAGNAME = "default";
    /**
     * the default value for the corresponding function.
     */
    private String value;
    /**
     * Complex type for the <default> element. default.type is mixed, that is, data may contain #PCDATA. Content type:
     * extension of data-extension.
     * type which is empty per default.
     * <p>
     * The name of this attribute in default is <b>default.type</b>
     */
    private String defaultType;

    // Constructors
    public GraphmlDefault() {
    }

    public GraphmlDefault(String value, String defaultType) {
        this.value = value;
        this.defaultType = defaultType;
    }

    // Builder
    public static GraphmlDefaultBuilder builder() {
        return new GraphmlDefaultBuilder();
    }

    public static class GraphmlDefaultBuilder {
        private String value;
        private String defaultType;

        public GraphmlDefaultBuilder value(String value) {
            this.value = value;
            return this;
        }

        public GraphmlDefaultBuilder defaultType(String defaultType) {
            this.defaultType = defaultType;
            return this;
        }

        public GraphmlDefault build() {
            return new GraphmlDefault(value, defaultType);
        }
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
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
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (defaultType != null) attributes.put("default.type", defaultType);

        return attributes;
    }

}
