package com.calpano.graphinout.base.graphml;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element.
 * A node is declared with a node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GraphmlHyperEdge {@link GraphmlHyperEdge}
 */

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class GraphmlGraph extends GraphmlGraphCommonElement implements XMLValue {

    public enum EdgeDefault {
        directed,undirected
    }

    public static final String TAGNAME = "graph";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    private EdgeDefault edgedefault;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    private String id;


    /**
     * This is an Element that can be empty or null.
     * <p/>
     * The name of this Element in graph is <b>locator</b>
     */
    private GraphmlLocator locator;


    public GraphmlGraph(String id, EdgeDefault edgedefault) {
       super();
        this.edgedefault = edgedefault;
        this.id = id;
    }


    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if(id!=null && id.length() > 0) {
            attributes.put("id", String.valueOf(id));
        }
        attributes.put("edgedefault",String.valueOf(edgedefault));
        return attributes;
    }

}
