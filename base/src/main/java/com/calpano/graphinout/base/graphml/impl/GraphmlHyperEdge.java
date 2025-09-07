package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDescAndId;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;

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
 *         &lt;hyperedge id="id--hyperedge-4N56"&gt;
 *             &lt;desc&gt;bla bla&lt;/desc&gt;
 *             &lt;endpoint node="id--node4" type="in" port="North"/&gt;
 *             &lt;endpoint node="id--node5" type="in"/&gt;
 *             &lt;endpoint node="id--node6" type="in"/&gt;
 *         &lt;/hyperedge&gt;
 * </pre>
 * <p>
 * See also {@link GraphmlEdge}
 */
public class GraphmlHyperEdge extends GraphmlElementWithDescAndId implements IGraphmlHyperEdge {


    private final List<IGraphmlEndpoint> endpoints;

    public GraphmlHyperEdge(String id, Map<String, String> attributes, IGraphmlDescription desc, List<IGraphmlEndpoint> endpoints) {
        super(attributes, id, desc);
        this.endpoints = endpoints;
    }

    @Override
    public List<IGraphmlEndpoint> endpoints() {
        return endpoints;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlHyperEdge that = (IGraphmlHyperEdge) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that) //
                && Objects.equals(endpoints, that.endpoints());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, endpoints);
    }

    @Override
    public String toString() {
        return "GraphmlHyperEdge{" + "id='" + id + '\'' + ", endpoints=" + endpoints + ", desc=" + desc() + ", custom=" + customXmlAttributes() + '}';
    }

}
