package com.graphinout.reader.graphml.elements.impl;

import com.graphinout.reader.graphml.cj.CjGraphmlMapping;
import com.graphinout.reader.graphml.elements.GraphmlDataType;
import com.graphinout.reader.graphml.elements.GraphmlKeyForType;
import com.graphinout.reader.graphml.elements.IGraphmlDefault;
import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithDescAndId;
import com.graphinout.reader.graphml.elements.IGraphmlKey;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static com.graphinout.foundation.util.Nullables.nonNullOrDefault;

/**
 * @author rbaba
 * @implNote A GraphML-Attribute is defined by a key element which specifies the identifier, name, type and domain of
 * the attribute.
 * <p>
 * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all
 * GraphML-Attributes declared in the document. The purpose of the name is that applications can identify the meaning of
 * the attribute. Note that the name of the GraphML-Attribute is not used inside the document, the identifier is used
 * for this purpose.
 * <p>
 * The type of the GraphML-Attribute can be either boolean, int, long, float, double, or string.
 * <p>
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to the
 * whole collection of graphs described by the content of &lt;graphml&gt;. These functions are declared by &lt;key&gt;
 * elements (children of &lt;graphml&gt;) and defined by &lt;data&gt; elements. Occurence: &lt;graphml&gt;.
 * @see GraphmlData {@link GraphmlData}
 */
public class GraphmlKey extends GraphmlElementWithDescAndId implements IGraphmlKey {

    /**
     * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all
     * GraphML-Attributes declared in the document.
     * <p/>
     * * The name of this attribute in key is <b>attr.name</b>
     */
    private final String attrName;
    /**
     * The name of this attribute in key is <b>attr.type</b>
     */
    private final String attrType;
    /**
     * describes the domain of definition for the corresponding graph attribute.
     * <p>
     * Simple type for the for attribute of &lt;key&gt;. key.for.type is a restriction of xs:NMTOKEN Allowed values:
     * all, graph, node, edge, hyperedge, port and endpoint.
     * <p>
     * The name of this attribute in key is <b>for</b>
     */
    private final @Nullable GraphmlKeyForType forType;
    /**
     * This is an Element
     * <p>
     * The name of this attribute in key is <b>default</b>
     */
    private final @Nullable IGraphmlDefault defaultValue;

    /**
     * @param extraAttrib  optional
     * @param id           required
     * @param desc         optional
     * @param attrName     if not defined, defaults to 'id' value
     * @param attrType     if not defined, defaults to 'string'
     * @param forType      if not defined, defaults to 'all'
     * @param defaultValue optional
     */
    public GraphmlKey(@Nullable Map<String, String> extraAttrib, String id, IGraphmlDescription desc, //
                      @Nullable String attrName, @Nullable String attrType, @Nullable GraphmlKeyForType forType, @Nullable IGraphmlDefault defaultValue) {
        super(extraAttrib, id, desc);
        this.attrName = nonNullOrDefault(attrName, id);
        this.attrType = nonNullOrDefault(attrType, GraphmlDataType.typeString.graphmlName);
        this.forType = nonNullOrDefault(forType, GraphmlKeyForType.All);
        this.defaultValue = defaultValue;
    }

    @Override
    public String attrName() {
        return attrName;
    }

    @Override
    public String attrType() {
        return attrType;
    }

    @Override
    public @Nullable IGraphmlDefault defaultValue() {
        return defaultValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlKey that = (IGraphmlKey) o;
        return IGraphmlElementWithDescAndId.isEqual(this, that)//
                && Objects.equals(attrName, that.attrName()) //
                && Objects.equals(attrType, that.attrType()) //
                && Objects.equals(forType, that.forType()) //
                && Objects.equals(defaultValue, that.defaultValue());
    }

    @Override
    public GraphmlKeyForType forType() {
        return forType == null ? GraphmlKeyForType.All : forType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, attrName, attrType, forType, defaultValue);
    }

    public boolean is(CjGraphmlMapping.GraphmlDataElement graphmlDataElement) {
        return attrName.equals(graphmlDataElement.attrName);
    }


    @Override
    public String toString() {
        return "GraphmlKey{" + "id='" + id + '\'' + ", attrName='" + attrName + '\'' + ", attrType='" + attrType + '\'' + ", forType=" + forType + ", defaultValue=" + defaultValue + ", desc=" + desc() + ", custom=" + customXmlAttributes() + '}';
    }

}
