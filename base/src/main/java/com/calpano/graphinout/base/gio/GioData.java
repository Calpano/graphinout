package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to
 * the whole collection of graphs described by the content of <graphml>. These functions are declared by <key> elements
 * (children of <graphml>) and defined by <data> elements.
 * Occurence: <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 *
 * @author rbaba

 * @implNote <p>
 * Structured content can be added within the data element.
 * If you want to add structured content to graph elements you should use the key/data extension mechanism of GraphML.
 * @see GioKey {@link GioKey}
 */

public class GioData extends GioElement {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in data is <b>id</b>
     */
    private @Nullable String id;
    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private String value;
    /**
     * Links this data instance to a definition given in a {@link GioKey} element
     * <p>
     * TODO validate: Must refer to a previously defined {@link GioKey#getId()}. -- similar to #90
     */
    private String key;

    // Constructors
    public GioData() {
        super();
    }

    public GioData(@Nullable String id, String value, String key) {
        super();
        this.id = id;
        this.value = value;
        this.key = key;
    }

    // Builder
    public static GioDataBuilder builder() {
        return new GioDataBuilder();
    }

    public static class GioDataBuilder {
        private @Nullable String id;
        private String value;
        private String key;

        public GioDataBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

        public GioDataBuilder value(String value) {
            this.value = value;
            return this;
        }

        public GioDataBuilder key(String key) {
            this.key = key;
            return this;
        }

        public GioData build() {
            return new GioData(id, value, key);
        }
    }

    // Getters and Setters
    public @Nullable String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioData gioData = (GioData) o;
        return Objects.equals(id, gioData.id) &&
               Objects.equals(value, gioData.value) &&
               Objects.equals(key, gioData.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, value, key);
    }

    @Override
    public String toString() {
        return "GioData{" +
               "id='" + id + '\'' +
               ", value='" + value + '\'' +
               ", key='" + key + '\'' +
               ", customAttributes=" + getCustomAttributes() +
               '}';
    }

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }
}
