package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each other,
 * they express a relation between an arbitrary number of enpoints.
 * Hyperedges are declared by a hyperedge element in GraphML.
 * For each enpoint of the hyperedge, this hyperedge element contains an endpoint element.
 * The endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document.
 * Note that edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * @see GioEdge {@link GioEdge}
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GioEndpoint extends GioElement  {

    /**
     * Edge id. Don't confuse with {@link #node}.
     *
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in endpoint is <b>id</b>
     */
    private String id;

    /**
     * This is an attribute is optional but dependent to port.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in endpoint is <b>node</b>
     * The value of this attribute points to an existing  node, and the ID of the desired node must be stored in this field.
     */
    private String node;

    /**
     * This is an attribute is optional but dependent to node.
     * In fact, one of the node or port values should be initialized.
     * </p>
     * The name of this attribute in endpoint is <b>port</b>
     * The value of this attribute points to an existing  port, and the name of the desired port must be stored in this field.
     */
    private @Nullable String port;

    /**
     * Defines the attribute for direction on this endpoint (undirected per default).
     * <p>
     * The name of this attribute in endpoint is <b>type</b>
     */
    @Builder.Default
    private GioEndpointDirecton type = GioEndpointDirecton.Undirected;
}
