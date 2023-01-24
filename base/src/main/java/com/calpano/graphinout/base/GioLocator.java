package com.calpano.graphinout.base;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.HashMap;
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
public class GioLocator implements XMLValue {

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

    @Override
    public String startTag() {

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (xLinkHref != null) attributes.put("xlink:herf", xLinkHref.toString());

        if (xLinkType != null) attributes.put("xlink:type", xLinkType);

        if (locatorExtraAttrib != null) attributes.put("locator.extra.attrib", locatorExtraAttrib);

        return GIOUtil.makeStartElement("locator", attributes);
    }

    @Override
    public String valueTag() {
        return "";
    }

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement("locator");
    }
}
