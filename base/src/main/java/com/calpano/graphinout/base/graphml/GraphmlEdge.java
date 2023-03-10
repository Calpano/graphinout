package com.calpano.graphinout.base.graphml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * @author rbaba
 * @version 0.0.1
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

@AllArgsConstructor
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
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

    @Override
    public String startTag() {
        return null;
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

    @Override
    public String valueTag() {
        return null;
    }

    @Override
    public String endTag() {
        return null;
    }
}