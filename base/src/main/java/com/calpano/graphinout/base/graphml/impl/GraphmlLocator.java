package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlLocator;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 *
 * <p>
 * Graphs and nodes are declared by the elements &lt;graph&gt; and &lt;node&gt;, respectively. The optional
 * &lt;locator&gt;-child of these elements point to their definition. (If there is no &lt;locator&gt;-child the
 * graphs/nodes are defined by their content). Occurence: &lt;graph&gt;, and &lt;node&gt;
 */
public class GraphmlLocator implements IGraphmlLocator {

    public static class GraphmlLocatorBuilder {

        private URL xLinkHref;

        public IGraphmlLocator build() {
            return new GraphmlLocator(xLinkHref);
        }

        public GraphmlLocatorBuilder xLinkHref(URL xLinkHref) {
            this.xLinkHref = xLinkHref;
            return this;
        }

    }

    /**
     * points to the resource of this locator. This is a mandatory attribute.
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

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlLocator that = (GraphmlLocator) o;
        return Objects.equals(xLinkHref, that.xLinkHref);
    }

    @Override
    public Map<String, String> attributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("xlink:href", xLinkHref.toExternalForm());
        // GraphML schema implies this attribute: attributes.put("xlink:type", "simple");
        return attributes;
    }

    @Override
    public String tagName() {
        return TAGNAME;
    }

    // Getters and Setters
    @Override
    public URL xlinkHref() {
        return xLinkHref;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xLinkHref);
    }

    public void setXLinkHref(URL xLinkHref) {
        this.xLinkHref = xLinkHref;
    }

    @Override
    public String toString() {
        return "GraphmlLocator{" +
                "xLinkHref=" + xLinkHref +
                '}';
    }

}
