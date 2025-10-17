package com.graphinout.base.graphml.impl;

import com.graphinout.base.graphml.IGraphmlDescription;
import com.graphinout.base.graphml.IGraphmlLocator;
import com.graphinout.base.graphml.IGraphmlNode;

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

public class GraphmlNode extends GraphmlElementWithDescAndId implements IGraphmlNode {

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private final IGraphmlLocator locator;

    public GraphmlNode(Map<String, String> attributes, @Nullable String id, @Nullable IGraphmlDescription desc, @Nullable IGraphmlLocator locator) {
        super(attributes, id, desc);
        this.locator = locator;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlNode that = (GraphmlNode) o;
        return Objects.equals(id, that.id) && Objects.equals(locator, that.locator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, locator);
    }

    public IGraphmlLocator locator() {
        return locator;
    }

    @Override
    public String toString() {
        return "GraphmlNode{" + "id='" + id + '\'' + ", locator=" + locator + ", desc=" + desc() + ", custom=" + customXmlAttributes() + '}';
    }

}
