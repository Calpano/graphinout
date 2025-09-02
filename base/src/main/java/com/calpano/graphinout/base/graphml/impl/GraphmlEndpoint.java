package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDescAndId;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;

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
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 */
public class GraphmlEndpoint extends GraphmlElementWithDescAndId implements IGraphmlEndpoint {

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
    private GraphmlDirection type = GraphmlDirection.Undirected;

    public GraphmlEndpoint(Map<String, String> attributes, @Nullable String id, String node, @Nullable String port, GraphmlDirection type, @Nullable IGraphmlDescription desc) {
        super(attributes, id, desc);
        this.node = node;
        this.port = port;
        this.type = type != null ? type : GraphmlDirection.Undirected;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlEndpoint that = (IGraphmlEndpoint) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that) //
                && Objects.equals(node, that.node())  //
                && Objects.equals(port, that.port()) //
                && type == that.type();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), node, port, type);
    }

    @Override
    public String node() {
        return node;
    }

    @Nullable
    @Override
    public String port() {return port;}

    @Override
    public String toString() {
        return "GraphmlEndpoint{" + "id='" + id() + '\'' + ", node='" + node + '\'' + ", port='" + port + '\'' + ", type=" + type + ", desc=" + desc + ", custom=" + customXmlAttributes() + '}';
    }

    @Override
    public GraphmlDirection type() {
        return type;
    }

}
