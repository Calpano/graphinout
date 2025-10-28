package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.element.CjDataSchema;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjEdge;
import com.graphinout.base.cj.element.ICjHasData;
import com.graphinout.base.cj.element.ICjHasLabel;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.base.graphml.GraphmlDataType;
import com.graphinout.base.graphml.GraphmlKeyForType;
import com.graphinout.base.graphml.IGraphmlKey;
import com.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.util.PowerStreams;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class CjData2GraphmlKeyData {

    /**
     * Define which data types are used in this document.
     * <p>
     * We combine:
     *
     * <li>(1) De-serialized Graphml KEYs definitions (we should keep IDs as they are)</li>
     * <li>(2) Computed KEYs from CJ:data (should get keys distinct from Graphml)</li>
     * <li>(3) Built-in CJ KEYs to represent CJ features as Graphml data (ids are 'cj_"...). These can be omitted in
     * GraphMl documents not effectively using them.</li>
     */
    public static GraphmlSchema buildGraphmlSchema(ICjDocument cjDoc) {
        GraphmlSchema graphmlSchema = new GraphmlSchema();
        // == (1) De-serialized Graphml KEYs definitions (we should keep IDs as they are)
        {
            ifPresentAccept(cjDoc.data(), ICjData::jsonValue, jsonValue -> {
                if (jsonValue.isObject()) {
                    IJsonValue keysObject = jsonValue.asObject().get(CjGraphmlMapping.CjDataProperty.Keys.cjPropertyKey);
                    if (keysObject == null || !keysObject.isObject()) return;
                    GraphmlSchema loadedSchema = GraphmlSchema.fromJson(keysObject);
                    loadedSchema.keys().forEach(graphmlSchema::addKey);
                }
            });
        }
        // == (2) Computed KEYs from CJ:data (should get keys distinct from Graphml)
        {
            CjDataSchema cjSchema = CjDocuments.calcEffectiveSchemaForData(cjDoc);
            GraphmlSchema graphmlSchemaFromCjData = toGraphmlSchema(cjSchema);
            graphmlSchemaFromCjData.keys().forEach(key -> {

                // if loadedSchema contains same 'for' (TODO or 'all') and 'attr.name', verify the type
                IGraphmlKey existingKey = graphmlSchema.findKeyByForAndAttrName(key.forType(), key.attrName());
//                if(existingKey==null) {
//                    existingKey = graphmlSchema.findKeyByForAndAttrName(GraphmlKeyForType.All, key.attrName());
//                }
                if (existingKey != null) {
                    GraphmlDataType existingType = GraphmlDataType.fromString(existingKey.attrType());
                    GraphmlDataType keyType = GraphmlDataType.fromString(key.attrType());
                    if (existingType.canRepresent(keyType)) {
                        // just keep using that
                        return;
                    }
                }

                // conflict or not present: use effectively used key instead
                if (existingKey != null) {
                    graphmlSchema.removeKeyById(existingKey.id());
                }
                graphmlSchema.addKey(key);
            });
            // FIXME key names -- when are we duplicating a key here that is the same as a loaded one
        }
        // == (3) Built-in CJ KEYs to represent CJ features as Graphml data (ids are 'cj_"...)
        // if cj:baseUri is used, we need a Graphml <key> for that
        {
            ifPresentAccept(cjDoc.baseUri(), baseUri -> //
                    // value of baseUri is not relevant here
                    graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.BaseUri.toGraphmlKey()));

            boolean usesCjEdgeType = PowerStreams.filterMap(cjDoc.allElements(), ICjEdge.class)
                    .map(ICjEdge::edgeType).anyMatch(Objects::nonNull);
            if (usesCjEdgeType) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.EdgeType.toGraphmlKey());
            }

            boolean usesCjLabels =
                    PowerStreams.filterMap(cjDoc.allElements(), ICjHasLabel.class)
                            .map(ICjHasLabel::label).anyMatch(Objects::nonNull);
            if (usesCjLabels) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.Label.toGraphmlKey());
            }

            // prepare <key> for CJ:data (graphml needs it pre-declared)
            boolean usesCjData = PowerStreams.filterMap(cjDoc.allElements(), ICjHasData.class) //
                    .map(ICjHasData::data)
                    .filter(Objects::nonNull)
                    .map(ICjData::jsonValue)
                    .anyMatch(jsonValue -> {
                        if(jsonValue == null) {
                            // TODO how to deal with this?
                            return true;
                        }
                        if (jsonValue.isObject()) {
                            // graphml round-tripped data will get exported as native GraphML
                            // so does not count as usage of cj-data
                            IJsonObject o = jsonValue.asObject();
                            return o.properties().map(Map.Entry::getKey)
                                    .anyMatch(key -> !key.startsWith("graphml:"));
                        } else {
                            return true;
                        }
                    });
            if (usesCjData) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.CjJsonData.toGraphmlKey());
            }

            // are synthetic nodes used in this doc?
            if (CjDocument2Graphml.containsSyntheticNodes(cjDoc)) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.SyntheticNode.toGraphmlKey());
            }
        }

        return graphmlSchema;
    }

    static Stream<ICjData> findAllDatas(CjDocumentElement cjDoc) {
        return cjDoc.allElements().filter(e -> e instanceof ICjHasData).map(e -> ((ICjHasData) e).data()).filter(Objects::nonNull);
    }

    public static boolean mapsToIndividualGraphmlProperties(IJsonValue value) {
        if (!value.isObject()) return false;
        IJsonObject o = value.asObject();
        // are all properties primitive values?
        return o.properties().filter(e -> !e.getKey().startsWith("graphml:")).map(Map.Entry::getValue) //
                .allMatch(v -> IJsonValue.isPrimitive(v));
    }

    public static GraphmlSchema toGraphmlSchema(CjDataSchema cjSchema) {
        GraphmlSchema graphmlSchema = new GraphmlSchema();
        cjSchema.map().forEach((cjType, tree) -> {
            GraphmlKeyForType keyForType = switch (cjType) {
                case Edge -> GraphmlKeyForType.Edge;
                case Node -> GraphmlKeyForType.Node;
                case Graph -> GraphmlKeyForType.Graph;
                case Port -> GraphmlKeyForType.Port;
                case RootObject -> GraphmlKeyForType.Graphml;
                case Endpoint -> GraphmlKeyForType.Endpoint;
                default -> throw new IllegalStateException("Data on '" + cjType + "' cannot be represented in GraphML");
            };
            Map<String, GraphmlDataType> graphmlTypes = CjGraphmlMapping.toGraphmlDataTypes(tree);
            graphmlTypes.forEach((propertyKey, graphmlDataType) -> {
                if (propertyKey.startsWith("graphml:")) {
                    // these are representing data in CJ that has a native GraphML construct
                    // so no need to express additionally as <key> / <data>
                    return;
                }
                GraphmlKeyBuilder builder = IGraphmlKey.builder();
                builder.id(keyForType.graphmlName() + "-" + propertyKey);
                builder.attrName(propertyKey);
                builder.forType(keyForType);
                builder.attrType(graphmlDataType);
                // IMPROVE we could create keys for 'all' when used on 3+ types?
                graphmlSchema.addKey(builder.build());
            });
        });
        return graphmlSchema;
    }

}
