package com.calpano.graphinout.base.graphml;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class GraphmlDocument extends GraphmlGraphCommonElement {

    public static class GraphmlDocumentBuilder extends GraphmlGraphCommonElementBuilder {

        private ArrayList<GraphmlKey> keys;

        public GraphmlDocumentBuilder() {
            this.keys = new ArrayList<>();
        }

        @Override
        public GraphmlDocument build() {
            return new GraphmlDocument(keys, extraAttrib, desc);
        }

        public GraphmlDocumentBuilder clearKeys() {
            if (this.keys != null) {
                this.keys.clear();
            }
            return this;
        }

        @Override
        public GraphmlDocumentBuilder desc(GraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlDocumentBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        public GraphmlDocumentBuilder key(GraphmlKey key) {
            if (key != null) {
                if (this.keys == null) {
                    this.keys = new ArrayList<>();
                }
                this.keys.add(key);
            }
            return this;
        }

        public GraphmlDocumentBuilder keys(List<GraphmlKey> keys) {
            if (keys != null) {
                if (this.keys == null) {
                    this.keys = new ArrayList<>();
                }
                this.keys.addAll(keys);
            }
            return this;
        }

    }
    public static final String DEFAULT_GRAPHML_XSD_URL = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    public static final String TAGNAME = "graphml";
    /**
     * The graphml element, like all other GraphML elements, belongs to the namespace
     * http://graphml.graphdrawing.org/xmlns. For this reason we define this namespace as the default namespace in the
     * document by adding the XML Attribute xmlns="http://graphml.graphdrawing.org/xmlns" to it.
     */
    public static final String HEADER_XMLNS = "http://graphml.graphdrawing.org/xmlns";
    /**
     * The  attribute, xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance", defines xsi as the XML Schema namespace.
     */
    public static final String HEADER_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     * The  attribute, xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
     * http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" , defines the XML Schema location for all elements in the
     * GraphML namespace.
     */
    public static final String HEADER_XMLNS_XSI_SCHEMA_LOCATIOM = "http://graphml.graphdrawing.org/xmlns " + DEFAULT_GRAPHML_XSD_URL;
    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graphML is <b>key</b>
     */
    private @Nullable List<GraphmlKey> keys;

    // Constructors
    public GraphmlDocument() {
        super();
    }

    public GraphmlDocument(@Nullable List<GraphmlKey> keys) {
        super();
        this.keys = keys;
    }

    public GraphmlDocument(@Nullable List<GraphmlKey> keys, GraphmlDescription desc) {
        super(desc);
        this.keys = keys;
    }

    public GraphmlDocument(@Nullable List<GraphmlKey> keys, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.keys = keys;
    }

    // Builder
    public static GraphmlDocumentBuilder builder() {
        return new GraphmlDocumentBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlDocument that = (GraphmlDocument) o;
        return Objects.equals(keys, that.keys);
    }

    // Getters and Setters
    public @Nullable List<GraphmlKey> getKeys() {
        return keys;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keys);
    }

    public void setKeys(@Nullable List<GraphmlKey> keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "GraphmlDocument{" +
                "keys=" + keys +
                ", desc=" + desc +
                ", extraAttrib=" + extraAttrib +
                '}';
    }

}
