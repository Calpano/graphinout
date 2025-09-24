package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDesc;
import com.calpano.graphinout.foundation.xml.XML;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.calpano.graphinout.foundation.xml.AttMaps.getOrDefault;

/**
 * @author rbaba
 * @implNote GraphML consists of a graphml element and a variety of subelements: graph, key, headers. GRAPHML is an XML
 * process instruction which defines that the document adheres to the XML 1.0 standard and that the encoding of the
 * document is UTF-8, the standard encoding for XML documents. Of course other encodings can be chosen for GraphML
 * documents.
 * <p>
 * The name of this Element in XML File is  <b>graphml</b>
 */
public class GraphmlDocument extends GraphmlElementWithDesc implements IGraphmlDocument {

    public GraphmlDocument(@Nullable Map<String, String> extraAttrib, @Nullable IGraphmlDescription desc) {
        super(extraAttrib, desc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IGraphmlDocument that = (IGraphmlDocument) o;
        return IGraphmlElementWithDesc.isEqual(this, that);
    }

    /**
     * XML namespaces and schema location
     * @param name_value (name, Supplier(@Nullable value))
     */
    public void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        // set XML Namespace to GraphML 1.0/1.1, if missing
        getOrDefault(xmlAttributes, XML.ATT_XMLNS, Graphml.XMLNS_GRAPHML_1_x, name_value);
        // set XML Schema Location namespace, if missing
        getOrDefault(xmlAttributes, XML.ATT_XMLNS_XSI, XML.XMLNS_XSI, name_value);
        // set schema location to GraphML 1.1, if missing
        getOrDefault(xmlAttributes, XML.ATT_XSI_SCHEMA_LOCATION, Graphml.XSI_SCHEMA_LOCATION_1_1, name_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    @Override
    public String toString() {
        return "GraphmlDocument{" + "desc=" + desc + ", custom=" + customXmlAttributes() + '}';
    }

}
