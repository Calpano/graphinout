package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.util.GIOUtil;
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

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement(TAGNAME);
    }

    @Override
    public Map<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xlink:herf", xLinkHref.toExternalForm());
        // TODO maybe can be omitted
        attributes.put("xlink:type", "simple");
        return attributes;
    }

    @Override
    public String startTag() {
        if (xLinkHref == null) throw new IllegalArgumentException();
        return GIOUtil.makeStartElement("locator", getAttributes());
    }

    @Override
    public String valueTag() {
        return "";
    }
}
