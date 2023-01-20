package com.calpano.graphinout.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

/**
 * @author rbaba
 * @version 0.0.1
 * <p>
 *     Graphs and nodes are declared by the elements <graph> and <node>, respectively.
 *     The optional <locator>-child of these elements point to their definition.
 *     (If there is no <locator>-child the graphs/nodes are defined by their content). Occurence: <graph>, and <node>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GioLocator {

    /**
     * points to the resource of this locator.
     * This is a mandatory attribute.
     * <p>
     * The name of this attribute is <b>xlink:herf</b>
     */
    private URL xLinkHref;

    /**
     * Type of the hyperlink (fixed as simple).
     * This is an optional attribute.
     * <p>
     * The name of this attribute is <b>xlink:type</b>
     */
    private String xLinkType;

    /**
     * user defined extra attributes for <locator> elements.
     * This is an optional attribute.
     * <p>
     * The name of this attribute is <b>locator.extra.attrib</b>
     */
    private String locatorExtraAttrib;

}
