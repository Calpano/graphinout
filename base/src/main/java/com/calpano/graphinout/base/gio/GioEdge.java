package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
 */
public class GioEdge extends GioElementWithDescription implements GioElementWithId {

    public static class GioEdgeBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable XmlFragmentString description;
        private String id;
        private List<GioEndpoint> endpoints = new ArrayList<>();

        public GioEdge build() {
            return new GioEdge(customAttributes, description, id, endpoints);
        }

        public GioEdgeBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioEdgeBuilder description(@Nullable XmlFragmentString description) {
            this.description = description;
            return this;
        }

        public GioEdgeBuilder endpoint(GioEndpoint endpoint) {
            if (this.endpoints == null) {
                this.endpoints = new ArrayList<>();
            }
            this.endpoints.add(endpoint);
            return this;
        }

        public GioEdgeBuilder endpoints(List<GioEndpoint> endpoints) {
            this.endpoints = endpoints != null ? new ArrayList<>(endpoints) : new ArrayList<>();
            return this;
        }

        public GioEdgeBuilder id(String id) {
            this.id = id;
            return this;
        }

    }

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in hyperEdge is <b>id</b>
     */
    private String id;
    /**
     * By convention, a simple directed edge should FIRST have the source, then the target endpoint.
     */
    private List<GioEndpoint> endpoints;


    public GioEdge() {
        super();
    }

    public GioEdge(String id, List<GioEndpoint> endpoints) {
        super();
        this.id = id;
        this.endpoints = endpoints;
    }

    public GioEdge(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description,
                   String id, List<GioEndpoint> endpoints) {
        super(customAttributes, description);
        this.id = id;
        this.endpoints = endpoints;
    }


    public static GioEdgeBuilder builder() {
        return new GioEdgeBuilder();
    }

    /**
     * By convention, a simple directed edge should FIRST have the source, then the target endpoint.
     */
    public void addEndpoint(GioEndpoint gioEndpoint) {
        if (endpoints == null) endpoints = new ArrayList<>();
        endpoints.add(gioEndpoint);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioEdge gioEdge = (GioEdge) o;
        return Objects.equals(id, gioEdge.id) &&
                Objects.equals(endpoints, gioEdge.endpoints);
    }

    public List<GioEndpoint> getEndpoints() {
        return endpoints;
    }


    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, endpoints);
    }

    public boolean isValid() {
        assert getEndpoints().size() >= 2 && getEndpoints().stream().allMatch(GioEndpoint::isValid);
        return true;
    }

    public void setEndpoints(List<GioEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GioEdge{" +
                "id='" + id + "''" +
                ", endpoints=" + endpoints +
                ", description='" + getDescription() + "''" +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
