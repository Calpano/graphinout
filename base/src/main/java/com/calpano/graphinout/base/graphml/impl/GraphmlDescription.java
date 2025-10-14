package com.calpano.graphinout.base.graphml.impl;

import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
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

    private final XmlFragmentString xmlValue;

    public GraphmlDescription(Map<String, String> attributes, XmlFragmentString xmlValue) {
        super(attributes);
        this.xmlValue = xmlValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlDescription that = (GraphmlDescription) o;
        return Objects.equals(xmlValue, that.xmlValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xmlValue);
    }

    @Override
    public String toString() {
        return "GraphmlDescription{" + "value='" + xmlValue + '\'' + " custom=" + customXmlAttributes() + "}";
    }

    public XmlFragmentString xmlValue() {
        return xmlValue;
    }

    @Override
    public void writeXml(XmlWriter xmlWriter) throws IOException {
        if (xmlValue == null) return;
        IXmlName xmlName = Graphml.xmlNameOf(tagName());
        if(xmlValue.xmlSpace() == XML.XmlSpace.preserve) {
            xmlWriter.elementStart(xmlName, Map.of(XML.XML_SPACE, xmlValue.xmlSpace().xmlAttValue));
        } else {
            xmlWriter.elementStart(xmlName);
        }
        xmlWriter.raw(xmlValue.rawXml());
        xmlWriter.elementEnd(xmlName);
    }

}
