package com.calpano.graphinout.base.gio;

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

public class GioPort extends GioElementWithDescription {

    public static class GioPortBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable String description;
        private String name;

        public GioPort build() {
            return new GioPort(customAttributes, description, name);
        }

        public GioPortBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioPortBuilder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public GioPortBuilder name(String name) {
            this.name = name;
            return this;
        }

    }
    /**
     * This is an attribute that mandatory.
     * </p>
     * The name of this attribute in graph is <b>name</b>
     */
    private final String name;

    // Constructors
    public GioPort(String name) {
        super();
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public GioPort(@Nullable Map<String, String> customAttributes, @Nullable String description, String name) {
        super(customAttributes, description);
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    // Builder
    public static GioPortBuilder builder() {
        return new GioPortBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioPort gioPort = (GioPort) o;
        return Objects.equals(name, gioPort.name);
    }

    // Getters (no setters for final field)
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "GioPort{" +
                "name='" + name + '\'' +
                ", description='" + getDescription() + '\'' +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }


}
