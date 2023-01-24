package com.calpano.graphinout.base;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

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
public class GioNode extends GioGraphCommonElement implements XMLValue {

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
     * The name of this Element in node is <b>graph</b>.
     */
    private GioGraph graph;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private GioLocator locator;

    @Override
    public String startTag() {

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        return GIOUtil.makeStartElement("node", attributes);
    }

    @Override
    public String valueTag() {
        StringBuilder xmlValueData = new StringBuilder();

        for (XMLValue xmlValue : ports) {
            xmlValueData.append(xmlValue.fullTag());
        }
        //TODO GRAPH?

        if (locator != null) xmlValueData.append(locator.fullTag());
        return xmlValueData.toString();
    }

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement("node");
    }
}
