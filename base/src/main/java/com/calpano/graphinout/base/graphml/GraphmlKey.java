package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote A GraphML-Attribute is defined by a key element which specifies the identifier, name, type and domain of the attribute.
 * <p>
 * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all GraphML-Attributes declared in the document.
 * The purpose of the name is that applications can identify the meaning of the attribute.
 * Note that the name of the GraphML-Attribute is not used inside the document, the identifier is used for this purpose.
 * <p>
 * The type of the GraphML-Attribute can be either boolean, int, long, float, double, or string.
 * <p>
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and
 * to the whole collection of graphs described by the content of <graphml>.
 * These functions are declared by <key> elements (children of <graphml>) and defined by <data> elements.
 * Occurence: <graphml>.
 * @see GraphmlData {@link GraphmlData}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GraphmlKey extends GraphmlGraphCommonElement  implements  XMLValue{

    public static final String TAGNAME = "key";
    /**
     * identifies this <key>
     * <p>
     * The name of this attribute in key is <b>id</b>
     */
    private String id;
    /**
     * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all
     * GraphML-Attributes declared in the document.
     * <p/>
     * * The name of this attribute in key is <b>attr.name</b>
     */
    private String attrName;
    /**
     * The name of this attribute in key is <b>attr.type</b>
     */
    private String attrType;

    /**
     * describes the domain of definition for the corresponding graph attribute.
     * <p>
     * Simple type for the for attribute of <key>.
     * key.for.type is a restriction of xs:NMTOKEN Allowed values: all, graph, node, edge, hyperedge, port and endpoint.
     * <p>
     * The name of this attribute in key is <b>for</b>
     */
    private GraphmlKeyForType forType;
    /**
     * This is an Element
     *
     * @see GraphmlDescription {@link  GraphmlDescription}
     */
    private GraphmlDescription desc;

    /**
     * This is an Element
     * <p>
     * The name of this attribute in key is <b>default</b>
     */
    private GraphmlDefault defaultValue;

    public void setForType(String forType) throws Exception {
        this.forType = GraphmlKeyForType.keyForType(forType);
    }

    @Override
    public String startTag() {
        LinkedHashMap<String, String> attributes = getAttributes();
        if (valueTag().isEmpty()) return GIOUtil.makeElement("key", attributes);
        return GIOUtil.makeStartElement("key", attributes);
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (attrName != null && !attrName.isEmpty()) attributes.put("attr.name", attrName);

        if (attrType != null && !attrType.isEmpty()) attributes.put("attr.type", attrType);

        if (forType != null) attributes.put("for", forType.name());

        attributes.putAll(extraAttrib);

        return  attributes;
    }

    @Override
    public String valueTag() {
        StringBuilder xmlValueData = new StringBuilder();
        if (desc != null) xmlValueData.append(desc.fullTag());
        if (getDataList() != null) for (XMLValue data : getDataList())
            xmlValueData.append(data.fullTag());
        if (defaultValue != null) {
            xmlValueData.append(defaultValue.fullTag());
        }
        //HIT GRAPH
        return xmlValueData.toString();
    }

    @Override
    public String endTag() {
        if (valueTag().isEmpty()) return "";
        return GIOUtil.makeEndElement("key");
    }
}

