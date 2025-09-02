package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlElement;

import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and
 * to the whole collection of graphs described by the content of &lt;graphml&gt;.
 * <p>
 * These functions are declared by &lt;key&gt; elements (children of &lt;graphml&gt;) and defined by &lt;data&gt;
 * elements. The (optional) &lt;default&gt; child of &lt;key&gt; gives the default value for the corresponding function.
 * Occurence: &lt;key&gt;.
 */
public class GraphmlDefault extends GraphmlElement implements IGraphmlDefault {

    /**
     * the default value for the corresponding function.
     */
    private final String value;

    public GraphmlDefault(Map<String, String> attributes, String value) {
        super(attributes);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlDefault that = (IGraphmlDefault) o;
        return IGraphmlElement.isEqual(this, that) && Objects.equals(value, that.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "GraphmlDefault{" + "value='" + value + '\'' + ", custom=" + customXmlAttributes() + '}';
    }

    @Override
    public String value() {
        return value;
    }

}
