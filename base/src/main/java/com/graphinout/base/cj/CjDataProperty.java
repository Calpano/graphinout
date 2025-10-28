package com.graphinout.base.cj;

import com.graphinout.base.cj.element.impl.CjDocumentElement;

/** Represent a property in the CJ "data" object */
public enum CjDataProperty {
    /**
     * Graphml: desc -> CJ: data.description. Graphml has a 'desc' element, CJ does not. So we map to a CJ data
     * property.
     */
    Description("graphml:description"),

    /**
     * Graphml splits data into {@code <key>} (id, attName, forType, attrType) and {@code <data>} (key-id, value). We
     * attach the key-data into each CJ data object.
     * <p>
     * <p>
     * Given
     * <code><pre>
     *     &lt;key id="$id1" attr.name="$name1" attr.type="$type1" for="$for1" /&gt;
     *     &lt;key id="$id2" attr.name="$name2" attr.type="$type2" for="$for2" /&gt;
     *     ...
     * </pre></code>
     * we add document level data:
     * <code><pre>
     *     "graphml:keys": "{
     *        "$id1": { "id":"$id1", "type":"$type1", "for":"$for1", "xml:attributes": { "foo":"bar" } }
     *        "$id2": { "id":"$id2", "type":"$type2", "for":"$for2" }
     *     }
     * </pre></code>
     *
     */
    Keys("graphml:keys"),

    /** A graphml {@code <data id+"...">}, which is irrelevant for processing. */
    DataId("graphml:id"),

    /**
     * Graphml: XML custom attributes -> CJ: data.cj:attributes. GraphML allows custom XML properties on every XML
     * element. CJ stores them in a JSON object in data.cj:attributes. Type: object.
     */
    CustomXmlAttributes(CjMappedProperties.XML_ATTRIBUTES),

    /** Graph-level default directedness of edges. Type: string. */
    EdgeDefault("graphml:edgeDefault"),

    /**
     * Transient marker for synthetic nodes which had been created in the first place in GraphML to represent
     * graph-graph nesting, which GraphMl does not allow, so graph-synthNode-graph is needed. Usually removed in
     * post-processing a {@link CjDocumentElement}. Type: boolean.
     */
    SyntheticNode("graphml:syntheticNode");

    public final String cjPropertyKey;

    CjDataProperty(String cjPropertyKey) {this.cjPropertyKey = cjPropertyKey;}

    public static boolean isCjPropertyKey(String keyId) {
        for (CjDataProperty value : values()) {
            if (value.cjPropertyKey.equals(keyId)) {
                return true;
            }
        }
        return false;
    }
}
