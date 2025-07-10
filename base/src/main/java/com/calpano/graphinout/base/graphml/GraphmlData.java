package com.calpano.graphinout.base.graphml;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote <p> Structured content can be added within the data element. If you want to add structured content to graph
 * elements you should use the key/data extension mechanism of GraphML.
 */

public class GraphmlData implements XMLValue {

    public static class GraphmlDataBuilder {

        private String id;
        private String value;
        private String key;

        public GraphmlData build() {
            return new GraphmlData(id, value, key);
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
            return this;
        }

    }
    public static final String TAGNAME = "data";
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

    // Constructors
    public GraphmlData() {
    }

    public GraphmlData(String id, String value, String key) {
        this.id = id;
        this.value = value;
        this.key = key;
    }

    public static GraphmlDataBuilder builder() {
        return new GraphmlDataBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlData that = (GraphmlData) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(value, that.value) &&
                Objects.equals(key, that.key);
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (key != null) attributes.put("key", key);


        return attributes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, key);
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
        return "GraphmlData{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

}
