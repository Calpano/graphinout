package com.calpano.graphinout.base.graphml;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A read-only GraphML element.
 */
public interface IGraphmlElement extends IXmlElement {

    /** DTD: "IMPLIED" */
    String ATTRIBUTE_ID = "id";

    /**
     // All attributes, except the built-in ones.
     * "Users can add attributes to all GraphML elements." User defined extra attributes, see
     * <a href="http://graphml.graphdrawing.org/specification.html">here</a>, bottom of page
     */
    default Map<String, String> customXmlAttributes() {
        Map<String, String> allXmlAttributes = new TreeMap<>( attributes() );
        builtInAttributeNames().forEach(allXmlAttributes::remove);
        return allXmlAttributes;
    }

    Set<String> builtInAttributeNames();

}
