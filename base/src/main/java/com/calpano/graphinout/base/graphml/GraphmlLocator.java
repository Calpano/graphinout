package com.calpano.graphinout.base.graphml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author rbaba
 * @version 0.0.1
 * <p>
 * Graphs and nodes are declared by the elements <graph> and <node>, respectively.
 * The optional <locator>-child of these elements point to their definition.
 * (If there is no <locator>-child the graphs/nodes are defined by their content). Occurence: <graph>, and <node>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GraphmlLocator implements XMLValue {

    public static final String TAGNAME = "locator";
    /**
     * points to the resource of this locator.
     * This is a mandatory attribute.
     * <p>
     * The name of this attribute is <b>xlink:href</b>
     */
    private URL xLinkHref;

    @Override
    public Map<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xlink:href", xLinkHref.toExternalForm());
        // GraphML schema implies this attribute: attributes.put("xlink:type", "simple");
        return attributes;
    }

}
