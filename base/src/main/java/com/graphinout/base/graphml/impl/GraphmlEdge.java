package com.graphinout.base.graphml.impl;

import com.graphinout.base.graphml.IGraphmlDescription;
import com.graphinout.base.graphml.IGraphmlEdge;
import com.graphinout.base.graphml.IGraphmlElementWithDescAndId;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

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
 *     &lt;!ELEMENT edge (desc?,data*,graph?)&gt;
 * &lt;!ATTLIST edge
 *           id         ID           #IMPLIED
 *           source     IDREF        #REQUIRED
 *           sourceport NMTOKEN      #IMPLIED
 *           target     IDREF        #REQUIRED
 *           targetport NMTOKEN      #IMPLIED
 *           directed   (true|false) #IMPLIED
 * &gt;
 * </pre>
 */

public class GraphmlEdge extends GraphmlElementWithDescAndId implements IGraphmlEdge {

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

    public GraphmlEdge(Map<String, String> attributes, @Nullable String id, @Nullable Boolean directed, String sourceId, String targetId, @Nullable String sourcePortId, @Nullable String targetPortId, @Nullable IGraphmlDescription desc) {
        super(attributes, id, desc);
        this.directed = directed;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourcePortId = sourcePortId;
        this.targetPortId = targetPortId;
    }


    @Nullable
    @Override
    public Boolean directed() {
        return directed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlEdge that = (IGraphmlEdge) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that) //
                && Objects.equals(directed, that.directed())  //
                && Objects.equals(sourceId, that.source()) //
                && Objects.equals(targetId, that.target()) //
                && Objects.equals(sourcePortId, that.sourcePort()) //
                && Objects.equals(targetPortId, that.targetPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), directed, sourceId, targetId, sourcePortId, targetPortId);
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
        return "GraphmlEdge{" + "id='" + id() + "''" + ", directed=" + directed + ", sourceId='" + sourceId + "''" + ", targetId='" + targetId + "''" + ", sourcePortId='" + sourcePortId + "''" + ", targetPortId='" + targetPortId + "''" + ", desc=" + desc + ", custom=" + customXmlAttributes() + '}';
    }

}
