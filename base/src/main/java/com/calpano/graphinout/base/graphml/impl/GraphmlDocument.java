package com.calpano.graphinout.base.graphml.impl;


import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlKey;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author rbaba
 * @implNote GraphML consists of a graphml element and a variety of subelements: graph, key, headers. GRAPHML is an XML
 * process instruction which defines that the document adheres to the XML 1.0 standard and that the encoding of the
 * document is UTF-8, the standard encoding for XML documents. Of course other encodings can be chosen for GraphML
 * documents.
 * <p>
 * xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd".
 * <p>
 * The name of this Element in XML File is  <b>graphml</b>
 */
public class GraphmlDocument extends GraphmlElement implements IGraphmlDocument {

    public static class GraphmlDocumentBuilder extends GraphmlElementBuilder {

        private ArrayList<IGraphmlKey> keys;

        public GraphmlDocumentBuilder() {
            this.keys = new ArrayList<>();
        }

        @Override
        public GraphmlDocument build() {
            return new GraphmlDocument(keys, attributes, desc);
        }

        public GraphmlDocumentBuilder clearKeys() {
            if (this.keys != null) {
                this.keys.clear();
            }
            return this;
        }

        @Override
        public GraphmlDocumentBuilder desc(IGraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlDocumentBuilder attributes(@Nullable Map<String, String> attributes) {
            super.attributes(attributes);
            return this;
        }

        public GraphmlDocumentBuilder key(IGraphmlKey key) {
            if (key != null) {
                if (this.keys == null) {
                    this.keys = new ArrayList<>();
                }
                this.keys.add(key);
            }
            return this;
        }

        public GraphmlDocumentBuilder keys(List<IGraphmlKey> keys) {
            if (keys != null) {
                if (this.keys == null) {
                    this.keys = new ArrayList<>();
                }
                this.keys.addAll(keys);
            }
            return this;
        }

    }

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graphML is <b>key</b>
     */
    private @Nullable List<IGraphmlKey> keys;

    // Constructors
    public GraphmlDocument() {
        super();
    }

    public GraphmlDocument(@Nullable List<IGraphmlKey> keys) {
        super();
        this.keys = keys;
    }

    public GraphmlDocument(@Nullable List<IGraphmlKey> keys, IGraphmlDescription desc) {
        super(desc);
        this.keys = keys;
    }

    public GraphmlDocument(@Nullable List<IGraphmlKey> keys, @Nullable Map<String, String> extraAttrib, IGraphmlDescription desc) {
        super(extraAttrib, desc);
        this.keys = keys;
    }

    @Override
    public Map<String, String> attributes() {
        return allAttributes != null ? new LinkedHashMap<>(allAttributes) : new LinkedHashMap<>();
    }

    @Override
    public Set<String> builtInAttributeNames() {
        return Set.of();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IGraphmlDocument that = (IGraphmlDocument) o;
        return Objects.equals(keys(), that.keys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keys);
    }

    // Getters and Setters
    @Nullable
    @Override
    public List<IGraphmlKey> keys() {
        return keys;
    }

    public void setKeys(@Nullable List<IGraphmlKey> keys) {
        this.keys = keys;
    }

    @Override
    public String tagName() {
        return TAGNAME;
    }

    @Override
    public String toString() {
        return "GraphmlDocument{" +
                "keys=" + keys +
                ", desc=" + desc +
                ", extraAttrib=" + allAttributes +
                '}';
    }

}
