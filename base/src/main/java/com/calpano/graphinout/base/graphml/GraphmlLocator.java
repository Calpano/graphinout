package com.calpano.graphinout.base.graphml;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba

 * <p>
 * Graphs and nodes are declared by the elements <graph> and <node>, respectively.
 * The optional <locator>-child of these elements point to their definition.
 * (If there is no <locator>-child the graphs/nodes are defined by their content). Occurence: <graph>, and <node>
 */
public class GraphmlLocator implements XMLValue {

    public static final String TAGNAME = "locator";
    /**
     * points to the resource of this locator.
     * This is a mandatory attribute.
     * <p>
     * The name of this attribute is <b>xlink:href</b>
     */
    private URL xLinkHref;

    // Constructors
    public GraphmlLocator() {
    }

    public GraphmlLocator(URL xLinkHref) {
        this.xLinkHref = xLinkHref;
    }

    // Builder
    public static GraphmlLocatorBuilder builder() {
        return new GraphmlLocatorBuilder();
    }

    public static class GraphmlLocatorBuilder {
        private URL xLinkHref;

        public GraphmlLocatorBuilder xLinkHref(URL xLinkHref) {
            this.xLinkHref = xLinkHref;
            return this;
        }

        public GraphmlLocator build() {
            return new GraphmlLocator(xLinkHref);
        }
    }

    // Getters and Setters
    public URL getXLinkHref() {
        return xLinkHref;
    }

    public void setXLinkHref(URL xLinkHref) {
        this.xLinkHref = xLinkHref;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlLocator that = (GraphmlLocator) o;
        return Objects.equals(xLinkHref, that.xLinkHref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xLinkHref);
    }

    @Override
    public String toString() {
        return "GraphmlLocator{" +
               "xLinkHref=" + xLinkHref +
               '}';
    }

    @Override
    public Map<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xlink:href", xLinkHref.toExternalForm());
        // GraphML schema implies this attribute: attributes.put("xlink:type", "simple");
        return attributes;
    }

}
