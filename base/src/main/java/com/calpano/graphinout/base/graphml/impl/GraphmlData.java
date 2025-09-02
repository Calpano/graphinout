package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithId;

import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote <p> Structured content can be added within the data element. If you want to add structured content to graph
 * elements you should use the key/data extension mechanism of GraphML.
 */

public class GraphmlData extends GraphmlElementWithId implements IGraphmlData {

    private final boolean isRawXml;
    /**
     * the value for any data, which can be extended to complex models like SVG.
     */
    private final String value;
    /**
     * This is an attribute is mandatory.
     * </p>
     * The name of this attribute in data is <b>key</b>
     */
    private final String key;

    public GraphmlData(String id, Map<String, String> attributes, String value, boolean isRawXml, String key) {
        super(attributes, id);
        this.value = value;
        this.key = key;
        this.isRawXml = isRawXml;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlData that = (IGraphmlData) o;
        return IGraphmlElementWithId.isEqual(this, that) //
                && Objects.equals(value, that.value()) //
                && Objects.equals(key, that.key())//
                && Objects.equals(isRawXml, that.isRawXml());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, key, isRawXml);
    }

    public boolean isRawXml() {
        return isRawXml;
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return "GraphmlData{" //
                + "value='" + value + '\'' + //
                ", isRawXml=" + isRawXml + //
                ", key='" + key + '\'' + //
                ", id='" + id() + '\'' + //
                ", custom=" + customXmlAttributes() + //
                '}';
    }

    public String value() {
        return value;
    }

}
