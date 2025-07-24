package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author rbaba
 * @implNote Edges in the graph are declared by the edge element. Each edge must define its two endpoints with the
 * XML-Attributes source and target. The value of the source, resp. target, must be the identifier of a node in the same
 * document.
 * <p>
 * Edges with only one endpoint, also called loops, selfloops, or reflexive edges, are defined by having the same value
 * for source and target.
 * <p>
 * The optional XML-Attribute directed declares if the edge is directed or undirected. The value true declares a
 * directed edge, the value false an undirected edge. If the direction is not explicitely defined, the default direction
 * is applied to this edge as defined in the enclosing graph.
 * <p>
 * Optionally an identifier for the edge can be specified with the XML Attribute id. When it is necessary to reference
 * the edge, the id XML-Attribute is used.
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 *
 * <pre>
 *     <!ELEMENT edge (desc?,data*,graph?)>
 * <!ATTLIST edge
 *           id         ID           #IMPLIED
 *           source     IDREF        #REQUIRED
 *           sourceport NMTOKEN      #IMPLIED
 *           target     IDREF        #REQUIRED
 *           targetport NMTOKEN      #IMPLIED
 *           directed   (true|false) #IMPLIED
 * >
 * </pre>
 */

public class GraphmlEdge extends GraphmlElement implements IGraphmlEdge {

    public static class GraphmlEdgeBuilder extends GraphmlElementBuilder {

        private @Nullable String id;
        private Boolean directed;
        private String sourceId;
        private String targetId;
        private String sourcePortId;
        private String targetPortId;

        @Override
        public GraphmlEdge build() {
            return new GraphmlEdge(id, directed, sourceId, targetId, sourcePortId, targetPortId, attributes, desc);
        }

        @Override
        public GraphmlEdgeBuilder desc(IGraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        public GraphmlEdgeBuilder directed(Boolean directed) {
            this.directed = directed;
            return this;
        }

        @Override
        public GraphmlEdgeBuilder attributes(@Nullable Map<String, String> attributes) {
            super.attributes(attributes);
            return this;
        }

        public GraphmlEdgeBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

        public GraphmlEdgeBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public GraphmlEdgeBuilder sourcePortId(String sourcePortId) {
            this.sourcePortId = sourcePortId;
            return this;
        }

        public GraphmlEdgeBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public GraphmlEdgeBuilder targetPortId(String targetPortId) {
            this.targetPortId = targetPortId;
            return this;
        }

    }

    public static final String TAGNAME = "edge";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    protected final @Nullable String id;
    /**
     * This is an attribute that can be true or false or null ot empty. The optional XML-Attribute directed declares if
     * the edge is directed or undirected. The value true declares a directed edge, the value false an undirected edge.
     * If the direction is not explicitely defined, the default direction is applied to this edge as defined in the
     * enclosing graph.
     * </p>
     * The name of this attribute in graph is <b>directed</b>
     */
    protected final @Nullable Boolean directed;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>source</b> which points to a node and the ID of the desired node is the
     * value of this attribute.
     */
    protected final String sourceId;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>target</b> which points to a node and the ID of the desired node is the
     * value of this attribute.
     */
    protected final String targetId;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>sourceport</b> which points to a port and the ID of the desired port is
     * the value of this attribute.
     */
    protected @Nullable String sourcePortId;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>targetport</b> which points to a port and the ID of the desired port is
     * the value of this attribute.
     */
    protected @Nullable String targetPortId;

    public GraphmlEdge(@Nullable String id, @Nullable Boolean directed, String sourceId, String targetId, @Nullable String sourcePortId, @Nullable String targetPortId, @Nullable Map<String, String> extraAttrib, @Nullable IGraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.directed = directed;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourcePortId = sourcePortId;
        this.targetPortId = targetPortId;
        this.allAttributes = extraAttrib;
    }

    @Override
    public LinkedHashMap<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>(allAttributes);
        if (id != null) attributes.put(IGraphmlEdge.ATTRIBUTE_ID, id);
        if (directed() != null) {
            attributes.put(ATTRIBUTE_DIRECTED, "" + directed());
        }
        attributes.put(ATTRIBUTE_SOURCE, source());
        attributes.put(ATTRIBUTE_TARGET, target());
        if (sourcePort() != null) {
            attributes.put(ATTRIBUTE_SOURCE_PORT, sourcePort());
        }
        if (targetPort() != null) {
            attributes.put(ATTRIBUTE_TARGET_PORT, targetPort());
        }
        if (customXmlAttributes() != null) attributes.putAll(customXmlAttributes());
        return attributes;
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of(IGraphmlEdge.ATTRIBUTE_ID, ATTRIBUTE_SOURCE, ATTRIBUTE_TARGET, ATTRIBUTE_DIRECTED, ATTRIBUTE_SOURCE_PORT, ATTRIBUTE_TARGET_PORT);
    }

    @Nullable
    @Override
    public Boolean directed() {
        return directed;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlEdge that = (GraphmlEdge) o;
        return Objects.equals(id, that.id) && Objects.equals(directed, that.directed) && Objects.equals(sourceId, that.sourceId) && Objects.equals(targetId, that.targetId) && Objects.equals(sourcePortId, that.sourcePortId) && Objects.equals(targetPortId, that.targetPortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, directed, sourceId, targetId, sourcePortId, targetPortId);
    }

    @Nullable
    @Override
    public String id() {
        return id;
    }

    @Override
    public String source() {
        return sourceId;
    }

    @Nullable
    @Override
    public String sourcePort() {
        return sourcePortId;
    }

    @Override
    public String tagName() {
        return TAGNAME;
    }

    @Override
    @Nullable
    public String target() {
        return targetId;
    }

    @Nullable
    @Override
    public String targetPort() {
        return targetPortId;
    }

    @Override
    public String toString() {
        return "GraphmlEdge{" + "id='" + id + '\'' + ", directed=" + directed + ", sourceId='" + sourceId + '\'' + ", targetId='" + targetId + '\'' + ", sourcePortId='" + sourcePortId + '\'' + ", targetPortId='" + targetPortId + '\'' + ", desc=" + desc + ", extraAttrib=" + allAttributes + '}';
    }

}
