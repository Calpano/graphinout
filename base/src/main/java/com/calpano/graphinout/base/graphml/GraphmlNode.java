package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
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

public class GraphmlNode extends GraphmlGraphCommonElement implements XMLValue {

    public static class GraphmlNodeBuilder extends GraphmlGraphCommonElementBuilder {

        private String id;
        private GraphmlLocator locator;

        @Override
        public GraphmlNode build() {
            return new GraphmlNode(id, locator, extraAttrib, desc);
        }

        @Override
        public GraphmlNodeBuilder desc(GraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlNodeBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        public GraphmlNodeBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlNodeBuilder locator(GraphmlLocator locator) {
            this.locator = locator;
            return this;
        }

    }
    public static final String TAGNAME = "node";
    /**
     * The identifier of a node is defined by the XML-Attribute id.
     * <b>This Attribute is mandatory.</b>
     * <p/>
     * The name of this attribute in graphMl is <b>id</b>.
     */
    private String id;
    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private GraphmlLocator locator;

    // Constructors
    public GraphmlNode() {
        super();
    }

    public GraphmlNode(String id) {
        super();
        this.id = id;
    }

    public GraphmlNode(String id, GraphmlLocator locator) {
        super();
        this.id = id;
        this.locator = locator;
    }

    public GraphmlNode(String id, GraphmlLocator locator, GraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.locator = locator;
    }

    public GraphmlNode(String id, GraphmlLocator locator, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.id = id;
        this.locator = locator;
    }

    // Builder
    public static GraphmlNodeBuilder builder() {
        return new GraphmlNodeBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlNode that = (GraphmlNode) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(locator, that.locator);
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);
        return attributes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public GraphmlLocator getLocator() {
        return locator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, locator);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocator(GraphmlLocator locator) {
        this.locator = locator;
    }

    @Override
    public String toString() {
        return "GraphmlNode{" +
                "id='" + id + '\'' +
                ", locator=" + locator +
                ", desc=" + desc +
                ", extraAttrib=" + extraAttrib +
                '}';
    }

}
