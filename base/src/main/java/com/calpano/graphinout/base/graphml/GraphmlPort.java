package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.XMLValue;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote A node may specify different logical locations for edges and hyperedges to connect.
 * The logical locations are called "ports".
 * As an analogy, think of the graph as a motherboard, the nodes as integrated circuits and the edges as connecting wires.
 * Then the pins on the integrated circuits correspond to ports of a node.
 * <p>
 * The ports of a node are declared by port elements as children of the corresponding node elements.
 * Note that port elements may be nested, i.e., they may contain port elements themselves.
 * Each port element must have an XML-Attribute name, which is an identifier for this port.
 * The edge element has optional XML-Attributes sourceport and targetport with which an edge may specify the port on
 * the source, resp. target, node. Correspondingly, the endpoint element has an optional XML-Attribute port.
 * <p>
 * Nodes may be structured by ports; thus edges are not only attached to a node but to a certain port in this node.
 * Occurence: <node>, <port>.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphmlPort implements XMLValue {

    /**
     * This is an attribute that mandatory.
     * </p>
     * The name of this attribute in graph is <b>name</b>
     */
    private String name;

    /**
     * User defined extra attributes for <port> elements.
     * <p>
     * The name of this attribute in port is <b>port.extra.attrib</b>
     */
    private String extraAttrib;

    /**
     * User defined ports for <port> elements.
     * <p>
     * The name of this attribute in port is <b>port</b>
     */
    private List<GraphmlPort> ports;

    @Override
    public String startTag() {

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (name != null && !name.isEmpty()) attributes.put("name", name);


        if (extraAttrib != null && !extraAttrib.isEmpty()) {
            attributes.put("port.extra.attrib", extraAttrib);
        }
        return GIOUtil.makeStartElement("port",attributes);
    }

    @Override
    public String valueTag() {
        StringBuilder xmlValueData = new StringBuilder();
        for (XMLValue xmlValue : ports) {
            xmlValueData.append(xmlValue.fullTag());
        }
        return xmlValueData.toString();
    }

    @Override
    public String endTag() {
        return GIOUtil.makeEndElement("port");
    }
}
