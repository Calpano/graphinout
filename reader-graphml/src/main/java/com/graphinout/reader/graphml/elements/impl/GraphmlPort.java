package com.graphinout.reader.graphml.elements.impl;

import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithDesc;
import com.graphinout.reader.graphml.elements.IGraphmlPort;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote A node may specify different logical locations for edges and hyperedges to connect. The logical locations
 * are called "ports". As an analogy, think of the graph as a motherboard, the nodes as integrated circuits and the
 * edges as connecting wires. Then the pins on the integrated circuits correspond to ports of a node.
 * <p>
 * The ports of a node are declared by port elements as children of the corresponding node elements. Note that port
 * elements may be nested, i.e., they may contain port elements themselves. Each port element must have an XML-Attribute
 * name, which is an identifier for this port. The edge element has optional XML-Attributes sourceport and targetport
 * with which an edge may specify the port on the source, resp. target, node. Correspondingly, the endpoint element has
 * an optional XML-Attribute port.
 * <p>
 * Nodes may be structured by ports; thus edges are not only attached to a node but to a certain port in this node.
 * Occurence: &lt;node&gt;, &lt;port&gt;.
 */

public class GraphmlPort extends GraphmlElementWithDesc implements IGraphmlPort {

    /**
     * This is an attribute that mandatory.
     * </p>
     * The name of this attribute in graph is <b>name</b>
     */
    private String name;

    public GraphmlPort(Map<String, String> attributes, @Nullable IGraphmlDescription desc, String name) {
        super(attributes, desc);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlPort that = (IGraphmlPort) o;
        return IGraphmlElementWithDesc.isEqual(this, that) //
                && Objects.equals(name, that.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }


    @Override
    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GraphmlPort{" + "name='" + name + '\'' + ", custom=" + customXmlAttributes() + '}';
    }

}
