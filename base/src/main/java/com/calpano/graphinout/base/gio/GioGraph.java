package com.calpano.graphinout.base.gio;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;


/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element.
 * A node is declared with a node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GioEdge {@link GioEdge}
 */

@SuperBuilder
@Data
public class GioGraph extends GioElementWithData {



    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    @Builder.Default
    private boolean edgedefault = false;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    private String id;

    public GioGraph(String id, boolean edgedefault) {
       super();
        this.edgedefault = edgedefault;
        this.id = id;
    }

}
