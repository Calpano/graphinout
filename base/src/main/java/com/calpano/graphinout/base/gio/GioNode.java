package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
@SuperBuilder
public class GioNode extends GioGraphCommonElement  {

    /**
     * The identifier of a node is defined by the XML-Attribute id.
     * <b>This Attribute is mandatory.</b>
     * <p/>
     * The name of this attribute in graphMl is <b>id</b>.
     */
    private String id;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>port</b>.
     */
    @Singular(ignoreNullCollections = true)
    private List<GioPort> ports;


    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private GioLocator locator;


}
