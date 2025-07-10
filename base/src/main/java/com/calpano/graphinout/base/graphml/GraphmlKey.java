package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
public class GraphmlKey extends GraphmlGraphCommonElement implements XMLValue {

    public static class GraphmlKeyBuilder extends GraphmlGraphCommonElementBuilder {

        private String id;
        private String attrName;
        private String attrType;
        private GraphmlKeyForType forType;
        private GraphmlDefault defaultValue;

        public GraphmlKeyBuilder attrName(String attrName) {
            this.attrName = attrName;
            return this;
        }

        public GraphmlKeyBuilder attrType(String attrType) {
            this.attrType = attrType;
            return this;
        }

        @Override
        public GraphmlKey build() {
            return new GraphmlKey(id, attrName, attrType, forType, defaultValue, extraAttrib, desc);
        }

        public GraphmlKeyBuilder defaultValue(GraphmlDefault defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public GraphmlKeyBuilder desc(GraphmlDescription desc) {
            super.desc(desc);
            return this;
        }

        @Override
        public GraphmlKeyBuilder extraAttrib(@Nullable Map<String, String> extraAttrib) {
            super.extraAttrib(extraAttrib);
            return this;
        }

        public GraphmlKeyBuilder forType(GraphmlKeyForType forType) {
            this.forType = forType;
            return this;
        }

        public GraphmlKeyBuilder id(String id) {
            this.id = id;
            return this;
        }

    }
    public static final String TAGNAME = "key";
    /**
     * identifies this &lt;key&gt;
     * <p>
     * The name of this attribute in key is <b>id</b>
     */
    private String id;
    /**
     * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all
     * GraphML-Attributes declared in the document.
     * <p/>
     * * The name of this attribute in key is <b>attr.name</b>
     */
    private String attrName;
    /**
     * The name of this attribute in key is <b>attr.type</b>
     */
    private String attrType;
    /**
     * describes the domain of definition for the corresponding graph attribute.
     * <p>
     * Simple type for the for attribute of &lt;key&gt;. key.for.type is a restriction of xs:NMTOKEN Allowed values:
     * all, graph, node, edge, hyperedge, port and endpoint.
     * <p>
     * The name of this attribute in key is <b>for</b>
     */
    private GraphmlKeyForType forType;
    /**
     * This is an Element
     * <p>
     * The name of this attribute in key is <b>default</b>
     */
    private GraphmlDefault defaultValue;

    // Constructors
    public GraphmlKey() {
        super();
    }

    public GraphmlKey(String id, String attrName, String attrType, GraphmlKeyForType forType,
                      GraphmlDescription desc, GraphmlDefault defaultValue) {
        super(desc);
        this.id = id;
        this.attrName = attrName;
        this.attrType = attrType;
        this.forType = forType;
        this.defaultValue = defaultValue;
    }

    public GraphmlKey(String id, String attrName, String attrType, GraphmlKeyForType forType,
                      GraphmlDefault defaultValue, @Nullable Map<String, String> extraAttrib, GraphmlDescription desc) {
        super(extraAttrib, desc);
        this.id = id;
        this.attrName = attrName;
        this.attrType = attrType;
        this.forType = forType;
        this.defaultValue = defaultValue;
    }

    // Builder
    public static GraphmlKeyBuilder builder() {
        return new GraphmlKeyBuilder();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GraphmlKey that = (GraphmlKey) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(attrName, that.attrName) &&
                Objects.equals(attrType, that.attrType) &&
                Objects.equals(forType, that.forType) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    public String getAttrName() {
        return attrName;
    }

    public String getAttrType() {
        return attrType;
    }

    @Override
    public LinkedHashMap<String, String> getAttributes() {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (id != null) attributes.put("id", id);

        if (attrName != null && !attrName.isEmpty()) attributes.put("attr.name", attrName);

        if (attrType != null && !attrType.isEmpty()) attributes.put("attr.type", attrType);

        if (forType != null) attributes.put("for", forType.value);

        if (extraAttrib != null)
            attributes.putAll(extraAttrib);

        return attributes;
    }

    public GraphmlDefault getDefaultValue() {
        return defaultValue;
    }

    public GraphmlKeyForType getForType() {
        return forType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, attrName, attrType, forType, defaultValue);
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }

    public void setDefaultValue(GraphmlDefault defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setForType(GraphmlKeyForType forType) {
        this.forType = forType;
    }

    public void setForType(String forType) throws Exception {
        this.forType = GraphmlKeyForType.keyForType(forType);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GraphmlKey{" +
                "id='" + id + '\'' +
                ", attrName='" + attrName + '\'' +
                ", attrType='" + attrType + '\'' +
                ", forType=" + forType +
                ", defaultValue=" + defaultValue +
                ", desc=" + desc +
                ", extraAttrib=" + extraAttrib +
                '}';
    }

}
