package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba

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

public class GraphmlPort extends GraphmlElement implements XMLValue {

    public static final String TAGNAME = "port";
    /**
     * This is an attribute that mandatory.
     * </p>
     * The name of this attribute in graph is <b>name</b>
     */
    private String name;

    // Constructors
    public GraphmlPort() {
        super();
    }

    public GraphmlPort(String name) {
        super();
        this.name = name;
    }

    public GraphmlPort(String name, @Nullable Map<String, String> extraAttrib) {
        super(extraAttrib);
        this.name = name;
    }

    // Builder
    public static GraphmlPortBuilder builder() {
        return new GraphmlPortBuilder();
    }

    public static class GraphmlPortBuilder extends GraphmlElementBuilder {
        private String name;

        public GraphmlPortBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public GraphmlPortBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        @Override
        public GraphmlPort build() {
            return new GraphmlPort(name, extraAttrib);
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlPort that = (GraphmlPort) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "GraphmlPort{" +
               "name='" + name + '\'' +
               ", extraAttrib=" + extraAttrib +
               '}';
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (name != null && !name.isEmpty()) attributes.put("name", name);
        if (getExtraAttrib() != null)
            attributes.putAll(getExtraAttrib());
        return attributes;
    }

}
