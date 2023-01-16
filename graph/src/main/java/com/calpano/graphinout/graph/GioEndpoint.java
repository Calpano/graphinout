package com.calpano.graphinout.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each other,
 * they express a relation between an arbitrary number of enpoints.
 * Hyperedges are declared by a hyperedge element in GraphML.
 * For each enpoint of the hyperedge, this hyperedge element contains an endpoint element.
 * The endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document.
 * Note that edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * @see GioHyperEdge {@link GioHyperEdge}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// TODO remove all @Xml annotations from model
public class GioEndpoint {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    protected String id;

    /**
     * This is an attribute is optional but dependent to port.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in graph is <b>node</b>
     * The value of this attribute points to an existing  node, and the ID of the desired node must be stored in this field.
     */
    private String node;

    /**
     * This is an attribute is optional but dependent to node.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in graph is <b>port</b>
     * The value of this attribute points to an existing  port, and the name of the desired port must be stored in this field.
     */
    protected String port;

    //TODO Check whether the existence of this attribute is true or not
    protected Direction type;

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>data</b>
     */
    private List<GioData> datas;


}
