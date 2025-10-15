package com.graphinout.base.graphml.impl;

import com.graphinout.base.graphml.IGraphmlElement;
import com.graphinout.base.graphml.IGraphmlLocator;

import java.net.URL;
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
public class GraphmlLocator extends GraphmlElement implements IGraphmlLocator {

    /**
     * points to the resource of this locator. This is a mandatory attribute.
     * <p>
     * The name of this attribute is <b>xlink:href</b>
     */
    private final URL xLinkHref;

    public GraphmlLocator(Map<String, String> attributes, URL xLinkHref) {
        super(attributes);
        this.xLinkHref = xLinkHref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlLocator that = (IGraphmlLocator) o;
        return IGraphmlElement.isEqual(this, that) //
                && Objects.equals(xLinkHref, that.xlinkHref());
    }

    @Override
    public int hashCode() {
        return Objects.hash(xLinkHref);
    }

    @Override
    public String toString() {
        return "GraphmlLocator{" + "xLinkHref=" + xLinkHref + ", custom=" + customXmlAttributes() + '}';
    }

    @Override
    public URL xlinkHref() {
        return xLinkHref;
    }

}
