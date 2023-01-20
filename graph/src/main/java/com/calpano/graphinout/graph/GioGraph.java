package com.calpano.graphinout.graph;

import lombok.*;

import java.util.List;


/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element.
 * A node is declared with a node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GioHyperEdge {@link GioHyperEdge}
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GioGraph  extends  GioGraphCommonElement{

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    // TODO maybe a boolean?
    private Direction edgedefault;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    private String id;
    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graph is <b>node</b>
     */
    @Singular(ignoreNullCollections = true)
    private List<GioNode> nodes;

    /**
     * This is an Element that can be empty or null.
     * <p/>
     * The name of this Element in graph is <b>hyperedge</b>
     * <p/>
     * All edges in the output will be converted to this element
     */
    @Singular(ignoreNullCollections = true)
    private List<GioHyperEdge> hyperEdges;

    /**
     * This is an Element that can be empty or null.
     * <p/>
     * The name of this Element in graph is <b>locator</b>
     */
    private GioLocator locator;

    public GioGraph(String id, String edgedefault) {
        this.edgedefault = Direction.getDirection(edgedefault);
        this.id = id;
    }

    public GioGraph(String id, Direction edgedefault) {
        this.edgedefault = edgedefault;
        this.id = id;
    }
}
