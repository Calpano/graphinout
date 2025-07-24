package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.GraphmlDocument;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGraphmlDocument extends IGraphmlWithDescElement {

    String DEFAULT_GRAPHML_XSD_URL = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    String TAGNAME = "graphml";
    /**
     * The graphml element, like all other GraphML elements, belongs to the namespace
     * http://graphml.graphdrawing.org/xmlns. For this reason we define this namespace as the default namespace in the
     * document by adding the XML Attribute xmlns="http://graphml.graphdrawing.org/xmlns" to it.
     */
    String HEADER_XMLNS = "http://graphml.graphdrawing.org/xmlns";
    /**
     * The  attribute, xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance", defines xsi as the XML Schema namespace.
     */
    String HEADER_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     * The  attribute, xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
     * http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" , defines the XML Schema location for all elements in the
     * GraphML namespace.
     */
    String HEADER_XMLNS_XSI_SCHEMA_LOCATIOM = "http://graphml.graphdrawing.org/xmlns " + DEFAULT_GRAPHML_XSD_URL;

    // Builder
    static GraphmlDocument.GraphmlDocumentBuilder builder() {
        return new GraphmlDocument.GraphmlDocumentBuilder();
    }

    Map<String, String> attributes();

    Set<String> builtInAttributeNames();

    IGraphmlDescription desc();

    // Getters and Setters
    @Nullable
    List<IGraphmlKey> keys();

    String tagName();

}
