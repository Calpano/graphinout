package com.calpano.graphinout.base.graphml;


import javax.annotation.Nullable;
import java.util.LinkedHashMap;
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

public class GraphmlGraph extends GraphmlGraphCommonElement implements XMLValue {

    public enum EdgeDefault {
        directed, undirected
    }

    public static class GraphmlGraphBuilder extends GraphmlGraphCommonElementBuilder {

        private EdgeDefault edgedefault;
        private String id;
        private GraphmlLocator locator;

        @Override
        public GraphmlGraph build() {
            return new GraphmlGraph(id, edgedefault, locator, extraAttrib, desc);
        }

        @Override
        public GraphmlGraphBuilder desc(GraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        public GraphmlGraphBuilder edgedefault(EdgeDefault edgedefault) {
            this.edgedefault = edgedefault;
            return this;
        }

        @Override
        public GraphmlGraphBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        public GraphmlGraphBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlGraphBuilder locator(GraphmlLocator locator) {
            this.locator = locator;
            return this;
        }

    }
    public static final String TAGNAME = "graph";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    private EdgeDefault edgedefault;
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
    private GraphmlLocator locator;

    // Constructors
    public GraphmlGraph() {
        super();
    }

    public GraphmlGraph(String id, EdgeDefault edgedefault) {
        super();
        this.edgedefault = edgedefault;
        this.id = id;
    }

    public GraphmlGraph(String id, EdgeDefault edgedefault, GraphmlLocator locator) {
        super();
        this.edgedefault = edgedefault;
        this.id = id;
        this.locator = locator;
    }

    public GraphmlGraph(String id, EdgeDefault edgedefault, GraphmlLocator locator, GraphmlDescription desc) {
        super(desc);
        this.edgedefault = edgedefault;
        this.id = id;
        this.locator = locator;
    }

    public GraphmlGraph(String id, EdgeDefault edgedefault, GraphmlLocator locator, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.edgedefault = edgedefault;
        this.id = id;
        this.locator = locator;
    }

    // Builder
    public static GraphmlGraphBuilder builder() {
        return new GraphmlGraphBuilder();
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
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null && id.length() > 0) {
            attributes.put("id", String.valueOf(id));
        }
        attributes.put("edgedefault", String.valueOf(edgedefault));
        return attributes;
    }

    // Getters and Setters
    public EdgeDefault getEdgedefault() {
        return edgedefault;
    }

    public String getId() {
        return id;
    }

    public GraphmlLocator getLocator() {
        return locator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgedefault, id, locator);
    }

    public void setEdgedefault(EdgeDefault edgedefault) {
        this.edgedefault = edgedefault;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocator(GraphmlLocator locator) {
        this.locator = locator;
    }

    @Override
    public String toString() {
        return "GraphmlGraph{" +
                "edgedefault=" + edgedefault +
                ", id='" + id + '\'' +
                ", locator=" + locator +
                ", desc=" + desc +
                ", extraAttrib=" + extraAttrib +
                '}';
    }

}
