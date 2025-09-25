package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;

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

        DataId("graphml:id"),

        /**
         * Graphml: XML custom attributes -> CJ: data.cj:attributes. GraphML allows custom XML properties on every XML
         * element. CJ stores them in a JSON object in data.cj:attributes.
         */
        CustomXmlAttributes("graphml:xmlAttributes"),

        /** Graph-level default directedness of edges. */
        EdgeDefault("graphml:edgeDefault"),

        /**
         * Transient marker for synthetic nodes which had been created in the first place in GraphML to represent
         * graph-graph nesting, which GraphMl does not allow, so graph-synthNode-graph is needed.
         * Usually removed in post-processing a {@link CjDocumentElement}.
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

        EdgeType("edgeType", GraphmlKeyForType.Edge, GraphmlDataType.typeString, "Connected JSON edgeType {'source'=..., 'type'=...}"),

        Label("label", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON label"),

        CjJsonData("cj:jsonData", GraphmlKeyForType.All, GraphmlDataType.typeString, "Connected JSON allows a JSON 'data' property on all objects."),

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

}
