package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * <p>
 * Provides human-readable descriptions for the GraphML element containing this &lt;desc&gt; as its first child.
 * Occurence: &lt;key&gt;, &lt;graphml&gt;, &lt;graph&gt;, &lt;node&gt;, &lt;port&gt;, &lt;edge&gt;, &lt;hyperedge&gt;,
 * and &lt;endpoint&gt;.
 */
public class GraphmlDescription extends GraphmlElement implements IGraphmlDescription {

    private final String value;

    public GraphmlDescription(Map<String, String> attributes, String value) {
        super(attributes);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlDescription that = (GraphmlDescription) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "GraphmlDescription{" + "value='" + value + '\'' + " custom=" + customXmlAttributes() + "}";
    }

    public String value() {
        return value;
    }

    @Override
    public void writeXml(XmlWriter xmlWriter) throws IOException {
        if (value == null) return;
        xmlWriter.elementStart(tagName());
        xmlWriter.raw(value);
        // xmlWriter.characterDataWhichMayContainCdata(value);
        xmlWriter.elementEnd(tagName());
    }

}
