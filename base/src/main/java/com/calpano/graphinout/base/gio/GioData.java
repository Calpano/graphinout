package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to the
 * whole collection of graphs described by the content of &lt;graphml&gt;. These functions are declared by &lt;key&gt;
 * elements (children of &lt;graphml&gt;) and defined by &lt;data&gt; elements. Occurence: &lt;graphml&gt;,
 * &lt;graph&gt;, &lt;node&gt;, &lt;port&gt;, &lt;edge&gt;, &lt;hyperedge&gt;, and &lt;endpoint&gt;.
 *
 * @author rbaba
 * @implNote <p> Structured content can be added within the data element. If you want to add structured content to graph
 * elements you should use the key/data extension mechanism of GraphML.
 * @see GioKey {@link GioKey}
 */

public class GioData extends GioElement {

    public static class GioDataBuilder {

        private @Nullable String id;
        private String value;
        private String key;

        public GioData build() {
            return new GioData(id, value, key);
        }

        public GioDataBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

        public GioDataBuilder key(String key) {
            this.key = key;
            return this;
        }

        public GioDataBuilder value(String value) {
            this.value = value;
            return this;
        }

    }

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


    public GioData() {
        super();
    }

    public GioData(@Nullable String id, String value, String key) {
        super();
        this.id = id;
        this.value = value;
        this.key = key;
    }


    public static GioDataBuilder builder() {
        return new GioDataBuilder();
    }


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


    public @Nullable String getId() {
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
        return Objects.hash(super.hashCode(), id, value, key);
    }

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    public void setId(@Nullable String id) {
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
        return "GioData{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
