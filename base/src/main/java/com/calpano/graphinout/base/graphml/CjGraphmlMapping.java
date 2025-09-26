package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.foundation.json.JsonType;

/**
 * Mapping data between CJ and GraphML models.
 */
public interface CjGraphmlMapping {

    /** Represent a property in the CJ "data" object */
    enum CjDataProperty {
        /**
         * Graphml: desc -> CJ: data.description. Graphml has a 'desc' element, CJ does not. So we map to a CJ data
         * property.
         */
        Description("graphml:description"),

        /** Graphml splits data into {@code <key>} (id, attName, forType, attrType) and {@code <data>} (key-id, value).
         * We attach the key-data into each CJ data object.
         * <p>
         *
         * TODO THIS IS NOT YET IMPLEMENTED
         * Given
         * <code>
         *     &lt;key id="$id1" attr.name="$name1" attr.type="$type1" for="$for1" /&gt;
         *     &lt;key id="$id2" attr.name="$name2" attr.type="$type2" for="$for2" /&gt;
         *     ...
         *     &lt;data key="$id1" value="$value1" /&gt;
         *     &lt;data key="$id2" value="$value2" /&gt;
         * </code>
         *  we map to
         * <code>
         *     {
         *         ...
         *         "data": {
         *             "$name1": "$value1",
         *             "$name2": "$value2",
         *             "graphml:key": {
         *                  "$name1": { "id":"$id1", "type":"$type1", "for":"$for1",
         *                              "xmlAttributes": { "foo":"bar" }
         *                              }
         *                  "$name2": { "id":"$id2", "type":"$type2", "for":"$for2" }
         *             }
         *         }
         *     }
         * </code>>
         * */
        Key("graphml:key"),

        /** A graphml {@code <data id+"...">}, which is irrelevant for processing. */
        DataId("graphml:id"),

        /**
         * Graphml: XML custom attributes -> CJ: data.cj:attributes. GraphML allows custom XML properties on every XML
         * element. CJ stores them in a JSON object in data.cj:attributes. Type: object.
         */
        CustomXmlAttributes("graphml:xmlAttributes"),

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
    }

    /** Represent a data element attached to a GraphML element. Needs a corresponding key element at document level. */
    enum GraphmlDataElement {
        /**
         * CJ: document.baseUri -> Graphml: We could use <a href="https://de.wikipedia.org/wiki/XML_Base">XHTML base
         * URI</a>, but currently simply map to GraphML data element.
         * <p>
         * {@code <key id="baseUri" for="graphml" value="http://example.org/"/>}
         */
        BaseUri("baseUri", GraphmlKeyForType.Graphml, GraphmlDataType.typeString, "Connected JSON allows custom JSON properties in all objects"),

        /** CJ edgeType as JSON (source, type) */
        EdgeType("edgeType", GraphmlKeyForType.Edge, GraphmlDataType.typeString, "Connected JSON edgeType {'source'=..., 'type'=...}"),

        /**
         * Export a CJ Label as a JSON Array [{"value":"Hallo","language":"de"}]. At import time, a plain text string is
         * accepted as label.value.
         */
        Label("label", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON label"),

        CjJsonData("cj:jsonData", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON allows a JSON 'data' property on all objects."),

        /** Required to represent CJ graph-graph nesting as Graphml graph-synthNode-graph */
        SyntheticNode("cj:syntheticNode", GraphmlKeyForType.Node, GraphmlDataType.typeBoolean, "Marks a node which only exists to wrap a CJ graph-in-graph");

        public final GraphmlKeyForType keyForType;
        public final String attrName;
        public final GraphmlDataType attrType;
        private final String desc;

        GraphmlDataElement(String attrName, GraphmlKeyForType graphmlKeyForType, GraphmlDataType attrType, String desc) {
            this.attrName = attrName;
            this.keyForType = graphmlKeyForType;
            this.attrType = attrType;
            this.desc = desc;
        }

        public IGraphmlData toGraphmlData(String valueString) {
            return new GraphmlDataBuilder()//
                    .key(attrName)//
                    .value(valueString)//
                    .build();
        }

        public IGraphmlKey toGraphmlKey() {
            return new GraphmlKeyBuilder()//
                    .id(attrName)//
                    .attrName(attrName)//
                    .forType(keyForType)//
                    .attrType(attrType.graphmlName)//
                    .desc(IGraphmlDescription.builder().value(desc).build())//
                    .build();
        }
    }

    static GraphmlDataType toGraphmlType(JsonType jsonType) {
        return switch (jsonType) {
            case String -> GraphmlDataType.typeString;
            case Number -> GraphmlDataType.typeDouble;
            case Boolean -> GraphmlDataType.typeBoolean;
            case Null -> GraphmlDataType.typeString; // GraphML has no null type, map to string
            default -> throw new IllegalArgumentException("Cannot map JSON type '" + jsonType + "' to GraphML type.");
        };
    }

}
