package com.calpano.graphinout.base.graphml;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each
 * other, they express a relation between an arbitrary number of endpoints. Hyperedges are declared by a hyperedge
 * element in GraphML. For each endpoint of the hyperedge, this hyperedge element contains an endpoint element. The
 * endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document. Note that
 * edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 */
public class GraphmlEndpoint implements XMLValue {

    public static class GraphmlEndpointBuilder {

        private String id;
        private String node;
        private String port;
        private GraphmlDirection type = GraphmlDirection.Undirected;
        private GraphmlDescription desc;

        public GraphmlEndpoint build() {
            return new GraphmlEndpoint(id, node, port, type, desc);
        }

        public GraphmlEndpointBuilder desc(GraphmlDescription desc) {
            this.desc = desc;
            return this;
        }

        public GraphmlEndpointBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GraphmlEndpointBuilder node(String node) {
            this.node = node;
            return this;
        }

        public GraphmlEndpointBuilder port(String port) {
            this.port = port;
            return this;
        }

        public GraphmlEndpointBuilder type(GraphmlDirection type) {
            this.type = type;
            return this;
        }

    }
    public static final String TAGNAME = "endpoint";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in endpoint is <b>id</b>
     */
    private String id;
    /**
     * This is an attribute is optional but dependent to port. In fact, one of the node or port values should be
     * initialized.
     * </p>
     * The name of this attribute in endpoint is <b>node</b> The value of this attribute points to an existing  node,
     * and the ID of the desired node must be stored in this field.
     */
    private String node;
    /**
     * This is an attribute is optional but dependent to node. In fact, one of the node or port values should be
     * initialized.
     * </p>
     * The name of this attribute in endpoint is <b>port</b> The value of this attribute points to an existing  port,
     * and the name of the desired port must be stored in this field.
     */
    private String port;
    /**
     * Defines the attribute for direction on this endpoint (undirected per default).
     * <p>
     * The name of this attribute in endpoint is <b>type</b>
     */
    private GraphmlDirection type = GraphmlDirection.Undirected;
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in endpoint is <b>desc</b>
     */
    private GraphmlDescription desc;

    // Constructors
    public GraphmlEndpoint() {
    }

    public GraphmlEndpoint(String id, String node, String port, GraphmlDirection type, GraphmlDescription desc) {
        this.id = id;
        this.node = node;
        this.port = port;
        this.type = type != null ? type : GraphmlDirection.Undirected;
        this.desc = desc;
    }

    // Builder
    public static GraphmlEndpointBuilder builder() {
        return new GraphmlEndpointBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlEndpoint that = (GraphmlEndpoint) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(node, that.node) &&
                Objects.equals(port, that.port) &&
                type == that.type &&
                Objects.equals(desc, that.desc);
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null) attributes.put("id", id);

        if (node != null) attributes.put("node", node);

        if (port != null) attributes.put("port", port);

        attributes.put("type", type.xmlValue());
        return attributes;
    }

    public GraphmlDescription getDesc() {
        return desc;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getNode() {
        return node;
    }

    public String getPort() {
        return port;
    }

    public GraphmlDirection getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, node, port, type, desc);
    }

    public void setDesc(GraphmlDescription desc) {
        this.desc = desc;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setType(GraphmlDirection type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "GraphmlEndpoint{" +
                "id='" + id + '\'' +
                ", node='" + node + '\'' +
                ", port='" + port + '\'' +
                ", type=" + type +
                ", desc=" + desc +
                '}';
    }

}
