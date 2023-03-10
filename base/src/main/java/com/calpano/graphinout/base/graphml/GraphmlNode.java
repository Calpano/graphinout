package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
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
public class GraphmlNode extends GraphmlGraphCommonElement implements XMLValue {

    public static final String TAGNAME = "node";
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
    @Deprecated
    private List<GraphmlPort> ports;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>graph</b>.
     */
    // FIXME this is wrong in a streaming API
    private GraphmlGraph graph;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in node is <b>locator</b>.
     */
    private GraphmlLocator locator;

    @Override
    public String startTag() {

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        return GIOUtil.makeStartElement("node", attributes);
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);
        return  attributes;
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
