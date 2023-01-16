package com.calpano.graphinout.graph;

import java.util.ArrayList;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote The <b>node</b> is an element.
 * <p>
 * Nodes in the graph are declared by the node element.
 * Each node has an identifier, which must be unique within the entire document, i.e.,
 * in a document there must be no two nodes with the same identifier.
 * The identifier of a node is defined by the XML-Attribute id.
 * <p>
 * The name of this Element in XML File is  <b>node</b>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioNode {

    /**
     * The identifier of a node is defined by the XML-Attribute id.
     * <b>This Attribute is mandatory.</b>
     * <p/>
     * The name of this attribute in graphMl is <b>id</b>.
     */
    protected String id;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graph is <b>data</b>.
     */
    protected List<GioData> datas;


    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graph is <b>port</b>.
     */
    @Singular(ignoreNullCollections = true)
    protected List<GioPort> ports;

    public void addData(GioData data){
        if(datas==null)
            datas=new ArrayList<>();
        datas.add(data);
    }

}
