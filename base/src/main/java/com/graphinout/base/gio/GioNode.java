package com.graphinout.base.gio;

import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote The <b>node</b> is an element.
 * <p>
 * Nodes in the graph are declared by the node element. Each node has an identifier, which must be unique within the
 * entire document, i.e., in a document there must be no two nodes with the same identifier. The identifier of a node is
 * defined by the XML-Attribute id.
 * <p>
 * The name of this Element in XML File is  <b>node</b>
 */
public class GioNode extends GioElementWithDescription implements GioElementWithId {

    public static class GioNodeBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable XmlFragmentString description;
        private String id;

        public GioNode build() {
            return new GioNode(customAttributes, description, id);
        }

        public GioNodeBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioNodeBuilder description(@Nullable XmlFragmentString description) {
            this.description = description;
            return this;
        }

        public GioNodeBuilder id(String id) {
            this.id = id;
            return this;
        }

    }

    /**
     * The identifier of a node is defined by the XML-Attribute id.
     * <b>This Attribute is mandatory.</b>
     * <p/>
     * The name of this attribute in graphMl is <b>id</b>.
     */
    private String id;


    public GioNode() {
        super();
    }

    public GioNode(String id) {
        super();
        this.id = id;
    }

    public GioNode(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description, String id) {
        super(customAttributes, description);
        this.id = id;
    }


    public static GioNodeBuilder builder() {
        return new GioNodeBuilder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioNode gioNode = (GioNode) o;
        return Objects.equals(id, gioNode.id);
    }


    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    public void setId(String id) {
        this.id = id;
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
