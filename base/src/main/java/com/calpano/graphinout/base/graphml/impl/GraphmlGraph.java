package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDescAndId;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;


/**
 * @author rbaba
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element. A node is declared with a
 * node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 */

public class GraphmlGraph extends GraphmlElementWithDescAndId implements IGraphmlGraph {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    private final EdgeDefault edgedefault;
    /**
     * This is an Element that can be empty or null.
     * <p/>
     * The name of this Element in graph is <b>locator</b>
     */
    private final IGraphmlLocator locator;

    public GraphmlGraph(String id, @Nullable EdgeDefault edgedefault, @Nullable IGraphmlLocator locator, Map<String, String> attributes, @Nullable IGraphmlDescription desc) {
        super(attributes, id, desc);
        this.edgedefault = edgedefault;
        this.locator = locator;
    }

    public EdgeDefault edgeDefault() {
        return edgedefault == null ? EdgeDefault.undirected : edgedefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlGraph that = (IGraphmlGraph) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that) //
                && edgedefault == that.edgeDefault()  //
                && Objects.equals(locator, that.locator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgedefault, id, locator);
    }

    @Override
    public IGraphmlLocator locator() {
        return locator;
    }

    @Override
    public String toString() {
        return "GraphmlGraph{" + "edgeDefault=" + edgedefault + ", id='" + id + '\'' + ", locator=" + locator + ", desc=" + desc() + ", custom=" + customXmlAttributes() + '}';
    }

}
