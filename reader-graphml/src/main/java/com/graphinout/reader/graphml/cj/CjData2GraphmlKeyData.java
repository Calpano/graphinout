package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDataSchema;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjHasLabel;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.stream.PowerStreams;
import com.graphinout.reader.graphml.elements.GraphmlDataType;
import com.graphinout.reader.graphml.elements.GraphmlKeyForType;
import com.graphinout.reader.graphml.elements.IGraphmlKey;
import com.graphinout.reader.graphml.elements.builder.GraphmlKeyBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

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
                    IJsonValue keysObject = jsonValue.asObject().get(CjDataProperty.Keys.cjPropertyKey);
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
            Map<String, GraphmlDataType> timeSeriesTypes = timeSeriesTypes(cjDoc);
            graphmlSchemaFromCjData.keys().toList().forEach(computedKey -> {
                // a time series is typed by its values, not as the array it is in CJ
                GraphmlDataType timeSeriesType = timeSeriesTypes.get(computedKey.forType().value + "|" + computedKey.attrName());
                IGraphmlKey key = timeSeriesType == null ? computedKey : withType(computedKey, timeSeriesType, true);

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
                    // keep everything of the original key but its type: id, desc, default, extra attributes like
                    // dynamic (issue #157)
                    key = withType(existingKey, GraphmlDataType.fromString(key.attrType()), false);
                }
                graphmlSchema.addKey(key);
            });
            // FIXME key names -- when are we duplicating a key here that is the same as a loaded one
        }
        // == (3) Built-in CJ KEYs to represent CJ features as Graphml data (ids are 'cj_"...)
        // if cj:@context is used, we need a Graphml <key> for that
        {
            ifPresentAccept(cjDoc.context(), context -> //
                    graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.Context.toGraphmlKey()));

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
                    .map(ICjData::jsonValue)
                    .anyMatch(jsonValue -> {
                        if (jsonValue == null) {
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

            if(cjDoc.nodesAll().anyMatch(node-> node.types().findAny().isPresent())) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.NodeTypes.toGraphmlKey());
            }

            if(cjDoc.edgesAll().flatMap(ICjEdgeChunk::endpoints).anyMatch(ep-> ep.type()!=null)) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.EndpointType.toGraphmlKey());
            }

            // are synthetic nodes used OR REQUIRED in this doc?
            if (CjDocument2Graphml.containsSyntheticNodes(cjDoc) || CjDocument2Graphml.requiresSyntheticNodes(cjDoc)) {
                graphmlSchema.addKey(CjGraphmlMapping.GraphmlDataElement.SyntheticNode.toGraphmlKey());
            }
        }

        return graphmlSchema;
    }

    /**
     * @param key     the key to copy
     * @param type    the new attr.type
     * @param dynamic true to mark the key as {@code dynamic="true"}, as it carries time-stamped values
     * @return a copy of the key with another type, keeping id, desc, default and extra XML attributes
     */
    static IGraphmlKey withType(IGraphmlKey key, GraphmlDataType type, boolean dynamic) {
        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        Map<String, String> attributes = new LinkedHashMap<>(key.customXmlAttributes());
        if (dynamic) attributes.putIfAbsent(GraphmlTimeValues.ATTRIBUTE_DYNAMIC, "true");
        builder.attributes(attributes);
        builder.id(key.id_());
        ifPresentAccept(key.attrName(), builder::attrName);
        builder.attrType(type);
        ifPresentAccept(key.forType(), builder::forType);
        ifPresentAccept(key.defaultValue(), builder::defaultValue);
        ifPresentAccept(key.desc(), builder::desc);
        return builder.build();
    }

    /**
     * For each property that holds a time series somewhere, the common GraphML type of all its values (time series
     * entries and plain values), keyed by {@code for|attr.name}. A property that also holds other non-primitive values
     * is left out: it is written as JSON anyway.
     */
    static Map<String, GraphmlDataType> timeSeriesTypes(ICjDocument cjDoc) {
        Map<String, List<IJsonPrimitive>> values = new LinkedHashMap<>();
        Set<String> withTimeSeries = new HashSet<>();
        Set<String> withOther = new HashSet<>();
        PowerStreams.filterMap(cjDoc.allElements(), ICjHasData.class).forEach(hasData -> {
            IJsonValue data = hasData.data().jsonValue();
            if (data == null || !data.isObject()) return;
            GraphmlKeyForType forType;
            try {
                forType = keyForType(hasData.asCjElement().cjType());
            } catch (IllegalStateException e) {
                return;
            }
            data.asObject().properties().forEach(e -> {
                if (e.getKey().startsWith("graphml:")) return;
                String id = forType.value + "|" + e.getKey();
                IJsonValue v = e.getValue();
                List<IJsonPrimitive> list = values.computeIfAbsent(id, k -> new ArrayList<>());
                if (GraphmlTimeValues.isTimeSeries(v)) {
                    withTimeSeries.add(id);
                    GraphmlTimeValues.entries(v).forEach(entry -> list.add(entry.value()));
                } else if (v.isPrimitive()) {
                    list.add(v.asPrimitive());
                } else {
                    withOther.add(id);
                }
            });
        });
        Map<String, GraphmlDataType> types = new LinkedHashMap<>();
        for (String id : withTimeSeries) {
            if (withOther.contains(id)) continue;
            types.put(id, CjGraphmlMapping.commonTypeFor(values.get(id).stream()));
        }
        return types;
    }

    static GraphmlKeyForType keyForType(CjType cjType) {
        return switch (cjType) {
            case Edge -> GraphmlKeyForType.Edge;
            case Node -> GraphmlKeyForType.Node;
            case Graph -> GraphmlKeyForType.Graph;
            case Port -> GraphmlKeyForType.Port;
            case RootObject -> GraphmlKeyForType.Graphml;
            case Endpoint -> GraphmlKeyForType.Endpoint;
            default -> throw new IllegalStateException("Data on '" + cjType + "' cannot be represented in GraphML");
        };
    }

    static Stream<ICjData> findAllDatas(CjDocumentElement cjDoc) {
        return cjDoc.allElements().filter(e -> e instanceof ICjHasData).map(e -> ((ICjHasData) e).data()).filter(Objects::nonNull);
    }

    public static boolean mapsToIndividualGraphmlProperties(IJsonValue value) {
        if (!value.isObject()) return false;
        IJsonObject o = value.asObject();
        // are all properties primitive values?
        return o.properties().filter(e -> !e.getKey().startsWith("graphml:")).map(Map.Entry::getValue) //
                .allMatch(v -> IJsonValue.isPrimitive(v) || GraphmlTimeValues.isTimeSeries(v));
    }

    public static GraphmlSchema toGraphmlSchema(CjDataSchema cjSchema) {
        GraphmlSchema graphmlSchema = new GraphmlSchema();
        cjSchema.map().forEach((cjType, tree) -> {
            GraphmlKeyForType keyForType = keyForType(cjType);
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
