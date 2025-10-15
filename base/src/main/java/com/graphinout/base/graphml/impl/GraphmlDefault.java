package com.graphinout.base.graphml.impl;

import com.graphinout.base.graphml.IGraphmlDefault;
import com.graphinout.base.graphml.IGraphmlElement;
import com.graphinout.foundation.xml.XmlFragmentString;

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
    private final XmlFragmentString xmlValue;

    public GraphmlDefault(Map<String, String> attributes, XmlFragmentString xmlValue) {
        super(attributes);
        this.xmlValue = xmlValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlDefault that = (IGraphmlDefault) o;
        return IGraphmlElement.isEqual(this, that) && Objects.equals(xmlValue, that.xmlValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), xmlValue);
    }

    @Override
    public String toString() {
        return "GraphmlDefault{" + "value='" + xmlValue + '\'' + ", custom=" + customXmlAttributes() + '}';
    }

    @Override
    public XmlFragmentString xmlValue() {
        return xmlValue;
    }

}
