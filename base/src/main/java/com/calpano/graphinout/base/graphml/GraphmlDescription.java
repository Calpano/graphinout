package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author rbaba

 * Provides human-readable descriptions for the GraphML element containing this <desc> as its first child.
 * Occurence: <key>, <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 */
public class GraphmlDescription implements XMLValue {
    private String value;

    // Constructors
    public GraphmlDescription() {
    }

    public GraphmlDescription(String value) {
        this.value = value;
    }

    // Builder
    public static GraphmlDescriptionBuilder builder() {
        return new GraphmlDescriptionBuilder();
    }

    public static class GraphmlDescriptionBuilder {
        private String value;

        public GraphmlDescriptionBuilder value(String value) {
            this.value = value;
            return this;
        }

        public GraphmlDescription build() {
            return new GraphmlDescription(value);
        }
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // equals, hashCode, toString
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
        return "GraphmlDescription{" +
               "value='" + value + '\'' +
               '}';
    }

    public void writeXml(XmlWriter xmlWriter) throws IOException {
        if(value==null)
            return;
        xmlWriter.startElement("desc");
        xmlWriter.characterData(value);
        xmlWriter.endElement("desc");
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        return null;
    }

}
