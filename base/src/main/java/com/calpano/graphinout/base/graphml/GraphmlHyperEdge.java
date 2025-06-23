package com.calpano.graphinout.base.graphml;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each
 * other, they express a relation between an arbitrary number of endpoints. Hyperedges are declared by a hyperedge
 * element in GraphML. For each endpoint of the hyperedge, this hyperedge element contains an endpoint element. The
 * endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document. Note that
 * edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * Example:
 * <pre>
 *         <hyperedge id="id--hyperedge-4N56">
 *             <desc>bla bla</desc>
 *             <endpoint node="id--node4" type="in" port="North"/>
 *             <endpoint node="id--node5" type="in"/>
 *             <endpoint node="id--node6" type="in"/>
 *         </hyperedge>
 * </pre>
 * <p>
 * See also {@link GraphmlEdge}
 */
public class GraphmlHyperEdge extends GraphmlGraphCommonElement implements XMLValue {

    public static class GraphmlHyperEdgeBuilder {
        private final ArrayList<GraphmlEndpoint> endpoints = new ArrayList<>();
        private final String id;

        public GraphmlHyperEdgeBuilder(String id) {
            this.id = id;
        }

        public GraphmlHyperEdgeBuilder addEndpoint(GraphmlEndpoint gioEndpoint) {
            endpoints.add(gioEndpoint);
            return this;
        }

        public GraphmlHyperEdge build() {
            if (endpoints.size() < 2)
                throw new IllegalStateException("Require at least 2 endpoints in hyperedge, got " + endpoints.size());
            return new GraphmlHyperEdge(id, endpoints);
        }
    }

    public static final String TAGNAME = "hyperedge";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in hyperEdge is <b>id</b>
     */
    private String id;

    private List<GraphmlEndpoint> endpoints;

    // Constructors
    public GraphmlHyperEdge() {
        super();
    }

    public GraphmlHyperEdge(String id, List<GraphmlEndpoint> endpoints) {
        super();
        this.id = id;
        this.endpoints = endpoints;
    }

    public GraphmlHyperEdge(String id, List<GraphmlEndpoint> endpoints, GraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.endpoints = endpoints;
    }

    public GraphmlHyperEdge(String id, List<GraphmlEndpoint> endpoints, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.id = id;
        this.endpoints = endpoints;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<GraphmlEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<GraphmlEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlHyperEdge that = (GraphmlHyperEdge) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, endpoints);
    }

    @Override
    public String toString() {
        return "GraphmlHyperEdge{" +
               "id='" + id + '\'' +
               ", endpoints=" + endpoints +
               ", desc=" + desc +
               ", extraAttrib=" + extraAttrib +
               '}';
    }

    public static GraphmlHyperEdgeBuilder builder(String id) {
        return new GraphmlHyperEdgeBuilder(id);
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null) attributes.put("id", id);
        if (getExtraAttrib() != null) attributes.putAll(getExtraAttrib());
        return attributes;
    }

}
