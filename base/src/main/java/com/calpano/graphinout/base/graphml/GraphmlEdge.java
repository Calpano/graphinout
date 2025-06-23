package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba

 * @implNote Edges in the graph are declared by the edge element.
 * Each edge must define its two endpoints with the XML-Attributes source and target.
 * The value of the source, resp. target, must be the identifier of a node in the same document.
 * <p>
 * Edges with only one endpoint, also called loops, selfloops, or reflexive edges, are defined by having the same value for source and target.
 * <p>
 * The optional XML-Attribute directed declares if the edge is directed or undirected.
 * The value true declares a directed edge, the value false an undirected edge.
 * If the direction is not explicitely defined, the default direction is applied to this edge as defined in the enclosing graph.
 * <p>
 * Optionally an identifier for the edge can be specified with the XML Attribute id.
 * When it is necessary to reference the edge, the id XML-Attribute is used.
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

public class GraphmlEdge extends GraphmlGraphCommonElement implements XMLValue {

    public static final String TAGNAME = "edge";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    protected @Nullable String id;

    /**
     * This is an attribute that can be true or false or null ot empty.
     * The optional XML-Attribute directed declares if the edge is directed or undirected.
     * The value true declares a directed edge, the value false an undirected edge.
     * If the direction is not explicitely defined, the default direction is applied to this edge as defined in the enclosing graph.
     * </p>
     * The name of this attribute in graph is <b>directed</b>
     */
    protected Boolean directed;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>source</b>
     * which points to a node and the ID of the desired node is the value of this attribute.
     */
    protected String sourceId;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>target</b>
     * which points to a node and the ID of the desired node is the value of this attribute.
     */
    protected String targetId;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>sourceport</b>
     * which points to a port and the ID of the desired port is the value of this attribute.
     */
    protected String sourcePortId;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>targetport</b>
     * which points to a port and the ID of the desired port is the value of this attribute.
     */
    protected String targetPortId;

    // Constructors
    public GraphmlEdge() {
        super();
    }

    public GraphmlEdge(@Nullable String id, Boolean directed, String sourceId, String targetId,
                      String sourcePortId, String targetPortId) {
        super();
        this.id = id;
        this.directed = directed;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourcePortId = sourcePortId;
        this.targetPortId = targetPortId;
    }

    public GraphmlEdge(@Nullable String id, Boolean directed, String sourceId, String targetId,
                      String sourcePortId, String targetPortId, GraphmlDescription desc) {
        super(desc);
        this.id = id;
        this.directed = directed;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourcePortId = sourcePortId;
        this.targetPortId = targetPortId;
    }

    public GraphmlEdge(@Nullable String id, Boolean directed, String sourceId, String targetId,
                      String sourcePortId, String targetPortId,
                      @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.id = id;
        this.directed = directed;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourcePortId = sourcePortId;
        this.targetPortId = targetPortId;
    }

    // Builder
    public static GraphmlEdgeBuilder builder() {
        return new GraphmlEdgeBuilder();
    }

    public static class GraphmlEdgeBuilder extends GraphmlGraphCommonElementBuilder {
        private @Nullable String id;
        private Boolean directed;
        private String sourceId;
        private String targetId;
        private String sourcePortId;
        private String targetPortId;

        public GraphmlEdgeBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

        public GraphmlEdgeBuilder directed(Boolean directed) {
            this.directed = directed;
            return this;
        }

        public GraphmlEdgeBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public GraphmlEdgeBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public GraphmlEdgeBuilder sourcePortId(String sourcePortId) {
            this.sourcePortId = sourcePortId;
            return this;
        }

        public GraphmlEdgeBuilder targetPortId(String targetPortId) {
            this.targetPortId = targetPortId;
            return this;
        }

        @Override
        public GraphmlEdgeBuilder desc(GraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlEdgeBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        @Override
        public GraphmlEdge build() {
            return new GraphmlEdge(id, directed, sourceId, targetId, sourcePortId, targetPortId, extraAttrib, desc);
        }
    }

    // Getters and Setters
    public @Nullable String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public Boolean getDirected() {
        return directed;
    }

    public void setDirected(Boolean directed) {
        this.directed = directed;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getSourcePortId() {
        return sourcePortId;
    }

    public void setSourcePortId(String sourcePortId) {
        this.sourcePortId = sourcePortId;
    }

    public String getTargetPortId() {
        return targetPortId;
    }

    public void setTargetPortId(String targetPortId) {
        this.targetPortId = targetPortId;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlEdge that = (GraphmlEdge) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(directed, that.directed) &&
               Objects.equals(sourceId, that.sourceId) &&
               Objects.equals(targetId, that.targetId) &&
               Objects.equals(sourcePortId, that.sourcePortId) &&
               Objects.equals(targetPortId, that.targetPortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, directed, sourceId, targetId, sourcePortId, targetPortId);
    }

    @Override
    public String toString() {
        return "GraphmlEdge{" +
               "id='" + id + '\'' +
               ", directed=" + directed +
               ", sourceId='" + sourceId + '\'' +
               ", targetId='" + targetId + '\'' +
               ", sourcePortId='" + sourcePortId + '\'' +
               ", targetPortId='" + targetPortId + '\'' +
               ", desc=" + desc +
               ", extraAttrib=" + extraAttrib +
               '}';
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null) attributes.put("id", id);
        attributes.put("source", getSourceId());
        attributes.put("target", getTargetId());
        attributes.put("directed", ""+ getDirected());
        if(getSourcePortId() != null) {
            attributes.put("sourceport", getSourcePortId());
        }
        if(getTargetPortId() != null) {
            attributes.put("targetport", getTargetPortId());
        }
        if (getExtraAttrib() != null)
            attributes.putAll(getExtraAttrib());
        return attributes;
    }

}
