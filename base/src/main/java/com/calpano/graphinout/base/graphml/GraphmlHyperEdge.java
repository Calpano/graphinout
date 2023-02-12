package com.calpano.graphinout.base.graphml;


import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote Hyperedges are a generalization of edges in the sense that they do not only relate two endpoints to each other,
 * they express a relation between an arbitrary number of enpoints.
 * Hyperedges are declared by a hyperedge element in GraphML.
 * For each enpoint of the hyperedge, this hyperedge element contains an endpoint element.
 * The endpoint element must have an XML-Attribute node, which contains the identifier of a node in the document.
 * Note that edges can be either specified by an edge element or by a hyperedge element containing two endpoint elements.
 * Example:
 * <pre>
 *         <hyperedge id="id--hyperedge-4N56">
 *             <desc>bla bla</desc>
 *             <endpoint node="id--node4" type="in" port="North"/>
 *             <endpoint node="id--node5" type="in"/>
 *             <endpoint node="id--node6" type="in"/>
 *         </hyperedge>
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GraphmlHyperEdge extends GraphmlGraphCommonElement implements XMLValue {

    // TODO add edge builder and allow only creation of hyperedges with >= 2 endpoints

    public static final String TAGNAME = "hyperedge";
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in hyperEdge is <b>id</b>
     */
    private String id;

    @Singular(ignoreNullCollections = true)
    private List<GraphmlEndpoint> endpoints;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in hyperEdge is <b>graph</b>.
     */
    private GraphmlGraph graph;


    public void addEndpoint(GraphmlEndpoint gioEndpoint) {
        if (endpoints == null) endpoints = new ArrayList<>();
        endpoints.add(gioEndpoint);
    }

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement("hyperEdge");
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (id != null) attributes.put("id", id);
        if (getExtraAttrib() != null)
            attributes.putAll(getExtraAttrib());
        return attributes;
    }

    @Override
    public String startTag() {
        LinkedHashMap<String, String> attributes = getAttributes();
        return GIOUtil.makeStartElement("hyperEdge", attributes);
    }

    /**
     * The graph must be stored in a different form, so it is not implemented here.
     * For large files, we don't want to keep the entire graph object in memory.
     *
     * @return
     */
    @Override
    public String valueTag() {
        final StringBuilder xmlValueData = new StringBuilder();
        if (desc != null) xmlValueData.append(desc.fullTag());
        for (XMLValue data : getDataList())
            xmlValueData.append(data.fullTag());
        for (XMLValue endpoints : getEndpoints())
            xmlValueData.append(endpoints.fullTag());
        //HIT GRAPH
        return xmlValueData.toString();
    }
}
