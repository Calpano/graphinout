package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlWithDescElement;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
public class GraphmlHyperEdge extends GraphmlElement implements IGraphmlHyperEdge {

    public static class GraphmlHyperEdgeBuilder {

        private final ArrayList<IGraphmlEndpoint> endpoints = new ArrayList<>();
        private final String id;
        private final Map<String, String> attributes = new HashMap<>();
        private IGraphmlDescription desc;

        public GraphmlHyperEdgeBuilder(String id) {
            this.id = id;
        }

        public GraphmlHyperEdgeBuilder addEndpoint(IGraphmlEndpoint gioEndpoint) {
            endpoints.add(gioEndpoint);
            return this;
        }

        public void attributes(Map<String, String> attributes) {
            this.attributes.putAll(attributes);
        }

        public GraphmlHyperEdge build() {
            if (endpoints.size() < 2)
                throw new IllegalStateException("Require at least 2 endpoints in hyperedge, got " + endpoints.size());
            return new GraphmlHyperEdge(id, endpoints);
        }

        public GraphmlHyperEdgeBuilder desc(IGraphmlDescription desc) {
            this.desc = desc;
            return this;
        }

        public boolean isBiEdge() {
            return endpoints.size() == 2;
        }

        public GraphmlEdge toEdge() {
            assert isBiEdge();

            GraphmlEdge.GraphmlEdgeBuilder builder = IGraphmlEdge.builder();
            builder.id(attributes.get(IGraphmlWithDescElement.ATTRIBUTE_ID));

            Map<String, String> atts = new HashMap<>(attributes);
            atts.remove(IGraphmlEdge.ATTRIBUTE_DIRECTED);
            builder.attributes(atts);

            builder.desc(desc);
            IGraphmlEndpoint source = endpoints.stream().filter(e -> e.type() == GraphmlDirection.In).findFirst().orElse(endpoints.get(0));
            IGraphmlEndpoint target = endpoints.stream().filter(e -> e.type() == GraphmlDirection.Out).findFirst().orElse(endpoints.get(1));

            // FIXME set only if different from the default
            builder.directed(source.type() != GraphmlDirection.Undirected);
            builder.sourceId(source.id());
            builder.sourcePortId(source.port());
            builder.targetId(target.id());
            builder.targetPortId(target.port());
            return builder.build();
        }

    }

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in hyperEdge is <b>id</b>
     */
    private String id;

    private List<IGraphmlEndpoint> endpoints;

    // Constructors
    public GraphmlHyperEdge() {
        super();
    }

    public GraphmlHyperEdge(String id, List<IGraphmlEndpoint> endpoints) {
        super();
        this.id = id;
        this.endpoints = endpoints;
    }

    public GraphmlHyperEdge(String id, List<IGraphmlEndpoint> endpoints, GraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.endpoints = endpoints;
    }

    public GraphmlHyperEdge(String id, List<IGraphmlEndpoint> endpoints, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.id = id;
        this.endpoints = endpoints;
    }

    @Override
    public LinkedHashMap<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null) attributes.put(ATTRIBUTE_ID, id);
        if (customXmlAttributes() != null) attributes.putAll(customXmlAttributes());
        return attributes;
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of(ATTRIBUTE_ID);
    }

    @Override
    public List<IGraphmlEndpoint> endpoints() {
        return endpoints;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlHyperEdge that = (GraphmlHyperEdge) o;
        return Objects.equals(id, that.id) && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, endpoints);
    }

    // Getters and Setters
    @Override
    public String id() {
        return id;
    }

    public void setEndpoints(List<IGraphmlEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String tagName() {
        return TAGNAME;
    }

    @Override
    public String toString() {
        return "GraphmlHyperEdge{" + "id='" + id + '\'' + ", endpoints=" + endpoints + ", desc=" + desc + ", extraAttrib=" + allAttributes + '}';
    }

}
