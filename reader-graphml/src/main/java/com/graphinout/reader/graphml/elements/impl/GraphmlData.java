package com.graphinout.reader.graphml.elements.impl;


import com.graphinout.reader.graphml.elements.IGraphmlData;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithId;
import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote <p> Structured content can be added within the data element. If you want to add structured content to graph
 * elements you should use the key/data extension mechanism of GraphML.
 */

public class GraphmlData extends GraphmlElementWithId implements IGraphmlData {

    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private final @Nullable XmlFragmentString xmlValue;
    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in data is <b>key</b>
     */
    private final String key;

    /**
     * @param attributes if xml:space is set, that must have been applied in the xmlValue
     * @param xmlValue must have the correct xmlSpace as the effective XmlSpace as defined by surrounding graph and maybe this element
     */
    public GraphmlData(String id, Map<String, String> attributes, @Nullable XmlFragmentString xmlValue, String key) {
        super(attributes, id);
        this.xmlValue = xmlValue;
        this.key = key;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlData that = (IGraphmlData) o;
        return IGraphmlElementWithId.isEqual(this, that) //
                && Objects.equals(xmlValue, that.xmlValue()) //
                && Objects.equals(key, that.key())//
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), xmlValue, key);
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return "GraphmlData{" +//
                "id='" + id() + '\'' + //
                ", key='" + key + '\'' + //
                ", custom=" + customXmlAttributes() + //
                ", xmlValue='" + xmlValue + '\'' + //
                '}';
    }

    public @Nullable XmlFragmentString xmlValue() {
        return xmlValue;
    }

}
