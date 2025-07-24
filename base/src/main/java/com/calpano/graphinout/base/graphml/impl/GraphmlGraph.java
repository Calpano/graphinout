package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author rbaba
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element. A node is declared with a
 * node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 */

public class GraphmlGraph extends GraphmlElement implements IGraphmlGraph {

    /**
     * The edgedefault attribute defines the default value of the edge attribute directed. The default value for
     * directed is directed.
     */
    public enum EdgeDefault {
        /** the default */
        directed, undirected
    }

    public static class GraphmlGraphBuilder extends GraphmlElementBuilder {

        private EdgeDefault edgedefault;
        private String id;
        private IGraphmlLocator locator;

        @Override
        public GraphmlGraph build() {
            return new GraphmlGraph(id, edgedefault, locator, attributes, desc);
        }

        @Override
        public GraphmlGraphBuilder desc(IGraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        public GraphmlGraphBuilder edgedefault(EdgeDefault edgedefault) {
            this.edgedefault = edgedefault;
            return this;
        }

        @Override
        public GraphmlGraphBuilder attributes(@Nullable Map<String, String> attributes) {
            super.attributes(attributes);
            return this;
        }

        public GraphmlGraphBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlGraphBuilder locator(IGraphmlLocator locator) {
            this.locator = locator;
            return this;
        }

    }

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    private @Nullable EdgeDefault edgedefault;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    private String id;
    /**
     * This is an Element that can be empty or null.
     * <p/>
     * The name of this Element in graph is <b>locator</b>
     */
    private IGraphmlLocator locator;

    public GraphmlGraph(String id, @Nullable EdgeDefault edgedefault, IGraphmlLocator locator, @Nullable Map<String, String> extraAttrib, IGraphmlDescription desc) {
        super(extraAttrib, desc);
        this.edgedefault = edgedefault;
        this.id = id;
        this.locator = locator;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlGraph that = (GraphmlGraph) o;
        return edgedefault == that.edgedefault &&
                Objects.equals(id, that.id) &&
                Objects.equals(locator, that.locator);
    }

    @Override
    public LinkedHashMap<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>(allAttributes);
        if (id != null && !id.isEmpty()) {
            attributes.put(ATTRIBUTE_ID, id);
        }
        attributes.put(ATTRIBUTE_EDGEDEFAULT, String.valueOf(edgedefault));
        return attributes;
    }

    @Override
    public String tagName() {
        return TAGNAME;
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of(ATTRIBUTE_ID, ATTRIBUTE_EDGEDEFAULT);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IGraphmlLocator getLocator() {
        return locator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgedefault, id, locator);
    }

    @Override
    public boolean isDirectedEdges() {
        // default is directed for null
        return edgedefault != EdgeDefault.undirected;
    }

    public void setEdgedefault(@Nullable EdgeDefault edgedefault) {
        this.edgedefault = edgedefault;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocator(IGraphmlLocator locator) {
        this.locator = locator;
    }

    @Override
    public String toString() {
        return "GraphmlGraph{" +
                "edgedefault=" + edgedefault +
                ", id='" + id + '\'' +
                ", locator=" + locator +
                ", desc=" + desc +
                ", extraAttrib=" + allAttributes +
                '}';
    }

}
