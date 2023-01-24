package com.calpano.graphinout.base.gio;


import java.util.*;

import com.calpano.graphinout.base.XMLValue;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

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
public class GioEdge extends GioGraphCommonElement {

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in hyperEdge is <b>id</b>
     */
    private String id;

    /**
     * User defined extra attributes for <hyperEdge> elements.
     * <p>
     * The name of this attribute in hyperEdge  is <b>hyperEdge.extra.attrib</b>
     */
    private String extraAttrib;

    @Singular(ignoreNullCollections = true)
    private List<GioEndpoint> endpoints;

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in hyperEdge is <b>graph</b>.
     */
    private GioGraph graph;


    public void addEndpoint(GioEndpoint gioEndpoint) {
        if (endpoints == null) endpoints = new ArrayList<>();
        endpoints.add(gioEndpoint);
    }

    @Override
    public String startTag() {

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (extraAttrib != null) attributes.put("hyperEdge.extra.attrib", extraAttrib);

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

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement("hyperEdge");
    }
}
