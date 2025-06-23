package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba

 * @implNote The <b>node</b> is an element.
 * <p>
 * Nodes in the graph are declared by the node element.
 * Each node has an identifier, which must be unique within the entire document, i.e.,
 * in a document there must be no two nodes with the same identifier.
 * The identifier of a node is defined by the XML-Attribute id.
 * <p>
 * The name of this Element in XML File is  <b>node</b>
 */
public class GioNode extends GioElementWithDescription {

    /**
     * The identifier of a node is defined by the XML-Attribute id.
     * <b>This Attribute is mandatory.</b>
     * <p/>
     * The name of this attribute in graphMl is <b>id</b>.
     */
    private String id;

    // Constructors
    public GioNode() {
        super();
    }

    public GioNode(String id) {
        super();
        this.id = id;
    }

    public GioNode(@Nullable Map<String, String> customAttributes, @Nullable String description, String id) {
        super(customAttributes, description);
        this.id = id;
    }

    // Builder
    public static GioNodeBuilder builder() {
        return new GioNodeBuilder();
    }

    public static class GioNodeBuilder {
        private @Nullable Map<String, String> customAttributes;
        private @Nullable String description;
        private String id;

        public GioNodeBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioNodeBuilder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public GioNodeBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GioNode build() {
            return new GioNode(customAttributes, description, id);
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioNode gioNode = (GioNode) o;
        return Objects.equals(id, gioNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "GioNode{" +
               "id='" + id + '\'' +
               ", description='" + getDescription() + '\'' +
               ", customAttributes=" + getCustomAttributes() +
               '}';
    }
}
