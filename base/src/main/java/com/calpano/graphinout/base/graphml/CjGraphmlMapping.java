package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.foundation.json.path.IJsonObjectNavigationStep;
import com.calpano.graphinout.foundation.json.path.JsonTypeAnalysisTree;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonXmlString;
import com.calpano.graphinout.foundation.json.value.JsonTypes;
import com.calpano.graphinout.foundation.util.ObjectRef;
import com.calpano.graphinout.foundation.util.PowerStreams;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.ObjectRef.objectRef;

/**
 * Mapping data between CJ and GraphML models.
 */
public interface CjGraphmlMapping {

    /** To be used as a JSON object property key */
    String XML_ATTRIBUTES = "graphml:xmlAttributes";

    /** Represent a property in the CJ "data" object */
    enum CjDataProperty {
        /**
         * Graphml: desc -> CJ: data.description. Graphml has a 'desc' element, CJ does not. So we map to a CJ data
         * property.
         */
        Description("graphml:description"),

        /**
         * Graphml splits data into {@code <key>} (id, attName, forType, attrType) and {@code <data>} (key-id, value).
         * We attach the key-data into each CJ data object.
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
        CustomXmlAttributes(XML_ATTRIBUTES),

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

    /** Represent a data element attached to a GraphML element. Needs a corresponding key element at document level. */
    enum GraphmlDataElement {
        /**
         * CJ: document.baseUri -> Graphml: We could use <a href="https://de.wikipedia.org/wiki/XML_Base">XHTML base
         * URI</a>, but currently simply map to GraphML data element.
         * <p>
         * {@code <key id="baseUri" for="graphml" value="http://example.org/"/>}
         */
        BaseUri("cj_baseUri", GraphmlKeyForType.Graphml, GraphmlDataType.typeString, "Connected JSON allows custom JSON properties in all objects"),

        /** CJ edgeType as JSON (source, type) */
        EdgeType("cj_edgeType", GraphmlKeyForType.Edge, GraphmlDataType.typeString, "Connected JSON edgeType {'source'=..., 'type'=...}"),

        /**
         * Export a CJ Label as a JSON Array [{"value":"Hallo","language":"de"}]. At import time, a plain text string is
         * accepted as label.value.
         */
        Label("cj_label", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON label"),

        CjJsonData("cj_jsonData", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON allows a JSON 'data' property on all objects."),

        /** Required to represent CJ graph-graph nesting as Graphml graph-synthNode-graph */
        SyntheticNode("cj_syntheticNode", GraphmlKeyForType.Node, GraphmlDataType.typeBoolean, "Marks a node which only exists to wrap a CJ graph-in-graph");

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
                    .attrType(attrType)//
                    .desc(IGraphmlDescription.of(desc))//
                    .build();
        }
    }


    static GraphmlDataType commonSuperTypeFor(Set<GraphmlDataType> gTypes) {
        assert !gTypes.isEmpty();
        if (gTypes.size() == 1) return gTypes.iterator().next();

        // if we have > 1 type, and one is boolean, only string can unite them
        if (gTypes.contains(GraphmlDataType.typeBoolean)) return GraphmlDataType.typeString;

        // if we have > 1 type, and one is string, only string can unite them
        if (gTypes.contains(GraphmlDataType.typeString)) return GraphmlDataType.typeString;

        // we have only number types
        if (gTypes.contains(GraphmlDataType.typeDouble)) return GraphmlDataType.typeDouble;
        if (gTypes.contains(GraphmlDataType.typeFloat)) return GraphmlDataType.typeFloat;
        if (gTypes.contains(GraphmlDataType.typeLong)) return GraphmlDataType.typeLong;
        return GraphmlDataType.typeInt;
    }

    static GraphmlDataType commonTypeFor(Stream<IJsonPrimitive> jsonPrimitives) {
        ObjectRef<GraphmlDataType> type = objectRef();

        jsonPrimitives.forEach(p -> {
            if (type.value == null) {
                type.value = toGraphmlType(p);
            } else {
                GraphmlDataType currentType = toGraphmlType(p);
                if (currentType != type.value) {
                    type.value = commonSuperTypeFor(Set.of(type.value, currentType));
                }
            }
        });

        return type.value;
    }

    /**
     * E.g. the data for all Nodes in a graph is described by aa single {@link JsonTypeAnalysisTree}. It will get
     * represented in Graphml as multiple {@code <key>} elements.
     * <p>
     * {@link IJsonXmlString} is {@link GraphmlDataType#typeString}.
     * <p>
     * (1) Convert Graphml data, which was represented as object properties with types bool, number, string, back to
     * Graphml. Use n keys, one per property. (2) Represent arbitrary JSON data as Graphml keys. (2a) primitives? array?
     * 1 key {@link GraphmlDataElement#CjJsonData} with any id.
     */
    static Map<String, GraphmlDataType> toGraphmlDataTypes(JsonTypeAnalysisTree tree) {
        Map<String, GraphmlDataType> propKey_graphmlType = new HashMap<>();

        // NOTE: Arrays are not used for type analysis
        PowerStreams.filterMap(tree.rootSteps().stream(), IJsonObjectNavigationStep.class).forEach(step -> {
            // convert all cj DATA object properties to Graphml <key>s
            String attrName = step.propertyKey();
            JsonTypeAnalysisTree.Node node = tree.get(step);
            if (node.isMapOfPrimitives()) {
                assert node.containerJsonType() == null;
                // what is the common super-type for all these primitives?
                Stream<IJsonPrimitive> jsonValues = node.distinctJsonPrimitiveValues();
                GraphmlDataType commonType = commonTypeFor(jsonValues);
                propKey_graphmlType.put(attrName, commonType);
            } else {
                assert node.containerJsonType() != null;
                // this is a nested json value or a typed string, use string
                propKey_graphmlType.put(attrName, GraphmlDataType.typeString);
            }
        });
        return propKey_graphmlType;
    }

    static GraphmlDataType toGraphmlType(IJsonPrimitive jsonValue) {
        return switch (jsonValue.jsonType()) {
            case String, XmlString -> GraphmlDataType.typeString;
            case Number -> {
                // we can map to more precise type. we know the actual value
                Number n = jsonValue.asNumber();
                Class<?> bestType = JsonTypes.strictestType(n);
                yield GraphmlDataType.fromJavaType(bestType);
            }
            case Boolean -> GraphmlDataType.typeBoolean;
            case Null -> GraphmlDataType.typeString; // GraphML has no null type, map to string
            default -> throw new IllegalArgumentException("Cannot map JSON value '" + jsonValue + "' to GraphML type.");
        };
    }


}
