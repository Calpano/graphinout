package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author rbaba
 * @implNote <p> Structured content can be added within the data element. If you want to add structured content to graph
 * elements you should use the key/data extension mechanism of GraphML.
 */

public class GraphmlData implements IGraphmlData {

    public static class GraphmlDataBuilder extends GraphmlElement.GraphmlElementBuilder {

        private String id;
        private String value;
        private boolean valueIsRawXml;
        private String key;

        public IGraphmlData build() {
            return new GraphmlData(id, value, valueIsRawXml, key);
        }

        public GraphmlDataBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlDataBuilder key(String key) {
            this.key = key;
            return this;
        }

        public GraphmlDataBuilder value(String value) {
            this.value = value;
            this.valueIsRawXml = false;
            return this;
        }

        public GraphmlDataBuilder valueRaw(String value) {
            this.value = value;
            this.valueIsRawXml = true;
            return this;
        }

    }

    private final boolean isRawXml;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in data is <b>id</b>
     */
    private String id;
    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private String value;
    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in data is <b>key</b>
     */
    private String key;

    public GraphmlData(String id, String value, boolean isRawXml, String key) {
        this.id = id;
        this.value = value;
        this.key = key;
        this.isRawXml = isRawXml;
    }

    public static GraphmlDataBuilder builder() {
        return new GraphmlDataBuilder();
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of(ATTRIBUTE_ID, ATTRIBUTE_KEY);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlData that = (GraphmlData) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value) && Objects.equals(key, that.key);
    }

    @Override
    public Map<String, String> attributes() {
        Map<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put(ATTRIBUTE_ID, id);

        if (key != null) attributes.put(ATTRIBUTE_KEY, key);


        return attributes;
    }


    // Getters and Setters
    public String id() {
        return id;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, key);
    }

    public boolean isRawXml() {
        return isRawXml;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "GraphmlData{" + "id='" + id + '\'' + ", value='" + value + '\'' + "(raw=" + isRawXml + "), key='" + key + '\'' + '}';
    }

}
