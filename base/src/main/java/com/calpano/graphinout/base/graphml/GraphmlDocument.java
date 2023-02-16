package com.calpano.graphinout.base.graphml;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote GraphML consists of a graphml element and a variety of subelements: graph, key, headers.
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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GraphmlDocument extends GraphmlGraphCommonElement {

    public static final String TAGNAME = "graphml";
    /**
     * The graphml element, like all other GraphML elements, belongs to the namespace http://graphml.graphdrawing.org/xmlns.
     * For this reason we define this namespace as the default namespace in the document by adding the XML Attribute
     * xmlns="http://graphml.graphdrawing.org/xmlns" to it.
     */
    public static final String HEADER_XMLNS = "http://graphml.graphdrawing.org/xmlns";

    /**
     * The  attribute, xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance", defines xsi as the XML Schema namespace.
     */
    public static final String HEADER_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";


    /**
     * The  attribute,
     * xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" ,
     * defines the XML Schema location for all elements in the GraphML namespace.
     */

    public static final String HEADER_XMLNS_XSI_SCHEMA_LOCATIOM = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graphML is <b>key</b>
     */
    @Singular(ignoreNullCollections = true)
    private @Nullable List<GraphmlKey> keys;


}
