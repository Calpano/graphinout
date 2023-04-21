package com.calpano.graphinout.base.graphml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;

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
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GraphmlPort extends GraphmlElement implements XMLValue {


    public static final String TAGNAME = "port";
    /**
     * This is an attribute that mandatory.
     * </p>
     * The name of this attribute in graph is <b>name</b>
     */
    private  String name;

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (name != null && !name.isEmpty()) attributes.put("name", name);
        if (getExtraAttrib() != null)
            attributes.putAll(getExtraAttrib());
        return attributes;
    }

}
