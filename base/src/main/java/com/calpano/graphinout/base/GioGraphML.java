package com.calpano.graphinout.base;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote GraphML consists of a graphml element and a variety of subelements: graph, key, heders.
 * GRAPHML is an XML process instruction which defines that the document
 * adheres to the XML 1.0 standard and that the encoding of the
 * document is UTF-8, the standard encoding for XML documents.
 * Of course other encodings can be chosen for GraphML documents.
 * <p>
 * xmlns="http://graphml.graphdrawing.org/xmlns"
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd".
 * <p>
 * The name of this Element in XML File is  <b>graphml</b>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioGraphML extends GioGraphCommonElement{

    /**
     * The graphml element, like all other GraphML elements, belongs to the namespace http://graphml.graphdrawing.org/xmlns.
     * For this reason we define this namespace as the default namespace in the document by adding the XML Attribute
     * xmlns="http://graphml.graphdrawing.org/xmlns" to it.
     */
    private final static String HEADER_XMLNS = "http://graphml.graphdrawing.org/xmlns";

    /**
     * The  attribute, xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance", defines xsi as the XML Schema namespace.
     */
    private final static String HEADER_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";


    /**
     * The  attribute,
     * xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" ,
     * defines the XML Schema location for all elements in the GraphML namespace.
     */

    private final static String HEADER_XMLNS_XSI_SCHEMA_LOCATIOM = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";


    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graphML is <b>id</b>
     */
    private String id;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graphML is <b>key</b>
     */
    @Singular(ignoreNullCollections = true)
    private List<GioKey> keys;

    /**
     * This is an Element that can be <i>empty</i> or <i>null</i>.
     * </p>
     * The name of this Element in graphML is <b>graph</b>
     */
    @Singular(ignoreNullCollections = true)
    private List<GioGraph> graphs;
}
