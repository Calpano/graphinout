package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each
 * other, they express a relation between an arbitrary number of endpoints. Hyperedges are declared by a hyperedge
 * element in GraphML. For each endpoint of the hyperedge, this hyperedge element contains an endpoint element. The
 * endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document. Note that
 * edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * @see GioEdge {@link GioEdge}
 */
public class GioEndpoint extends GioElement {

    public static class GioEndpointBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable String id;
        private String node;
        private @Nullable String port;
        private GioEndpointDirection type = GioEndpointDirection.Undirected;

        public GioEndpoint build() {
            return new GioEndpoint(customAttributes, id, node, port, type);
        }

        public GioEndpointBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioEndpointBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

        public GioEndpointBuilder node(String node) {
            this.node = node;
            return this;
        }

        public GioEndpointBuilder port(@Nullable String port) {
            this.port = port;
            return this;
        }

        public GioEndpointBuilder type(GioEndpointDirection type) {
            this.type = type;
            return this;
        }

    }
    /**
     * Endpoint id. Don't confuse with {@link #node}.
     * <p>
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in endpoint is <b>id</b>
     */
    private @Nullable String id;
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
    private @Nullable String port;
    /**
     * Defines the attribute for direction on this endpoint (undirected per default).
     * <p>
     * The name of this attribute in endpoint is <b>type</b>
     */
    private GioEndpointDirection type = GioEndpointDirection.Undirected;

    // Constructors
    public GioEndpoint() {
        super();
    }

    public GioEndpoint(@Nullable String id, String node, @Nullable String port, GioEndpointDirection type) {
        super();
        this.id = id;
        this.node = node;
        this.port = port;
        this.type = type != null ? type : GioEndpointDirection.Undirected;
    }

    public GioEndpoint(@Nullable Map<String, String> customAttributes, @Nullable String id, String node, @Nullable String port, GioEndpointDirection type) {
        super(customAttributes);
        this.id = id;
        this.node = node;
        this.port = port;
        this.type = type != null ? type : GioEndpointDirection.Undirected;
    }

    // Builder
    public static GioEndpointBuilder builder() {
        return new GioEndpointBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioEndpoint that = (GioEndpoint) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(node, that.node) &&
                Objects.equals(port, that.port) &&
                type == that.type;
    }

    // Getters and Setters
    public @Nullable String getId() {
        return id;
    }

    public String getNode() {
        return node;
    }

    public @Nullable String getPort() {
        return port;
    }

    public GioEndpointDirection getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, node, port, type);
    }

    public boolean isValid() {
        assert this.node != null : "endpoint.node is null";
        return true;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setPort(@Nullable String port) {
        this.port = port;
    }

    public void setType(GioEndpointDirection type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "GioEndpoint{" +
                "id='" + id + '\'' +
                ", node='" + node + '\'' +
                ", port='" + port + '\'' +
                ", type=" + type +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
