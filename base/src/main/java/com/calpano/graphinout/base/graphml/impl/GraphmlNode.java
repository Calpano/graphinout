package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.IGraphmlNode;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

public class GraphmlNode extends GraphmlElement implements IGraphmlNode {

    public static class GraphmlNodeBuilder extends GraphmlElementBuilder {

        private String id;
        private IGraphmlLocator locator;

        @Override
        public GraphmlNode build() {
            return new GraphmlNode(id, locator, attributes, desc);
        }

        @Override
        public GraphmlNodeBuilder desc(IGraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlNodeBuilder attributes(@Nullable Map<String, String> attributes) {
            super.attributes(attributes);
            return this;
        }

        public GraphmlNodeBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlNodeBuilder locator(IGraphmlLocator locator) {
            this.locator = locator;
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
    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private IGraphmlLocator locator;

    // Constructors
    public GraphmlNode() {
        super();
    }

    public GraphmlNode(String id) {
        super();
        this.id = id;
    }

    public GraphmlNode(String id, IGraphmlLocator locator) {
        super();
        this.id = id;
        this.locator = locator;
    }

    public GraphmlNode(String id, IGraphmlLocator locator, GraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.locator = locator;
    }

    public GraphmlNode(String id, IGraphmlLocator locator, @Nullable Map<String, String> extraAttrib, IGraphmlDescription desc) {
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
    public LinkedHashMap<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>(allAttributes);

        if (id != null) attributes.put(ATTRIBUTE_ID, id);
        return attributes;
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of(ATTRIBUTE_ID);
    }

    // Getters and Setters
    public String id() {
        return id;
    }

    public IGraphmlLocator locator() {
        return locator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, locator);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocator(IGraphmlLocator locator) {
        this.locator = locator;
    }

    @Override
    public String toString() {
        return "GraphmlNode{" +
                "id='" + id + '\'' +
                ", locator=" + locator +
                ", desc=" + desc +
                ", extraAttrib=" + allAttributes +
                '}';
    }

}
