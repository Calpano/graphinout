package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjHasData;
import com.calpano.graphinout.base.cj.element.ICjLabel;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.calpano.graphinout.base.graphml.GraphmlDataType;
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlParseInfo;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementWithDescBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.impl.GraphmlKey;
import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonTypedString;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import com.calpano.graphinout.foundation.util.MapSet;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;

/**
 * CJ to GraphML: {@link ICjDocument} to {@link GraphmlWriter}.
 */
public class CjDocument2Graphml {

    private final GraphmlWriter graphmlWriter;
    private final List<IGraphmlKey> keys = new ArrayList<>();

    public CjDocument2Graphml(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

//    // FIXME use it
//    public IGraphmlKey lookupKey(String id, GraphmlKeyForType forType) {
//        Map<GraphmlKeyForType, IGraphmlKey> subMap = dataId_for_key.getOrDefault(id, Collections.emptyMap());
//        return subMap.getOrDefault(
//                // 1) look up exact forType
//                forType,
//                // 2) look up ALL forType
//                subMap.get(GraphmlKeyForType.All));
//    }

    public static boolean mapsToIndividualGraphmlProperties(IJsonValue value) {
        if (!value.isObject()) return false;
        IJsonObject o = value.asObject();
        // are all properties primitive values?
        return o.properties().filter(e->!e.getKey().startsWith("graphml:")).map(Map.Entry::getValue) //
                .allMatch(v -> IJsonValue.isPrimitive(v) || IJsonTypedString.isTypedString(v));
    }

    public static void writeToGraphml(ICjDocument cjDoc, GraphmlWriter graphmlWriter) throws IOException {
        assert cjDoc != null;
        new CjDocument2Graphml(graphmlWriter).writeDocumentToGraphml(cjDoc);
    }

    public void writeCjLabelAsGraphmlData(@Nullable ICjLabel cjLabel) throws IOException {
        if (cjLabel == null) {
            return;
        }

        String value;
        if (cjLabel.entries().count() == 1 && cjLabel.entries().toList().getFirst().language() == null) {
            // represent as simple string
            value = cjLabel.entries().toList().getFirst().value();
        } else {
            // represent as JSON
            value = cjLabel.toJsonString();
        }

        this.graphmlWriter.data(GraphmlDataElement.Label.toGraphmlData(value));
    }

    /** Write given document to GraphML */
    public void writeDocumentToGraphml(ICjDocument cjDoc) throws IOException {
        assert cjDoc != null;
        GraphmlDocumentBuilder graphmlBuilder = IGraphmlDocument.builder();
        // map _SOME_ cjData to native graphMl constructs
        writeData_Description(cjDoc, graphmlBuilder);
        writeData_CustomAttributes(cjDoc, graphmlBuilder);

        // define which data types are used in this document
        if (cjDoc.baseUri() != null) {
            keys.add(GraphmlDataElement.BaseUri.toGraphmlKey());
        }
        keys.add(GraphmlDataElement.EdgeType.toGraphmlKey());
        keys.add(GraphmlDataElement.Label.toGraphmlKey());

        // are synthetic nodes used in this doc?
        boolean usesSyntheticNodes = findAllDatas((CjDocumentElement) cjDoc) //
                .map(ICjData::jsonValue).filter(Objects::nonNull) //
                .filter(IJsonValue::isObject).map(IJsonValue::asObject) //
                .anyMatch(o -> o.hasProperty(CjGraphmlMapping.CjDataProperty.SyntheticNode.cjPropertyKey));
        if (usesSyntheticNodes) {
            keys.add(GraphmlDataElement.SyntheticNode.toGraphmlKey());
        }

        MapSet<String, Object> usedTypes = MapSet.create();
        findAllDatas((CjDocumentElement) cjDoc) //
                .map(ICjData::jsonValue).filter(Objects::nonNull) //
                .filter(IJsonValue::isObject).map(IJsonValue::asObject) //
                .flatMap(IJsonObject::properties).forEach(prop -> {
                    IJsonValue value = prop.getValue();
                    Object type;
                    if(IJsonTypedString.isTypedString(value)) {
                        type = "TypedString";
                    } else {
                        type = value.jsonType();
                    }
                    usedTypes.add(prop.getKey(), type);
                });
        // add key elements for all simple usedTypes
        usedTypes.forEach((key, set) -> {
            if(key.startsWith("graphml:")) {
                // these are representing data in CJ that has a native GraphML construct,
                // so no need to express additionally as <key> / <data>
                return;
            }

            if (set.size() != 1) return;
            Object type = set.iterator().next();
            if(type instanceof JsonType jsonType) {
                if (jsonType.valueType() == JsonType.ValueType.Primitive) {
                    keys.add(new GraphmlKey(null, key, //
                            IGraphmlDescription.builder().value("cj-json to graphml").build(), //
                            key, CjGraphmlMapping.toGraphmlType(jsonType).graphmlName, GraphmlKeyForType.All, null));
                }
            } else if (type.equals("TypedString")) {
                keys.add(new GraphmlKey(null, key, //
                        IGraphmlDescription.builder().value("Typed string").build(), //
                        key, GraphmlDataType.typeString.graphmlName, GraphmlKeyForType.All, null));
            } else {
                throw new AssertionError("Unknown type: " + type);
            }
        });

        // prepare <key> for CJ:data (graphml needs it pre-declared)
        keys.add(GraphmlDataElement.CjJsonData.toGraphmlKey());

        // prepare <key> for CJ:baseUri (graphml has no baseUri)
        List<IGraphmlData> graphmlDatas = new ArrayList<>();
        ifPresentAccept(cjDoc.baseUri(), baseUri -> //
                graphmlDatas.add(GraphmlDataElement.BaseUri.toGraphmlData(baseUri)));

        // <!ELEMENT graphml  (desc?,key*,(data|graph)*)>
        graphmlWriter.documentStart(graphmlBuilder.build());
        for (IGraphmlKey key : keys) {
            graphmlWriter.key(key);
        }

        // emit graphml document level data
        for (IGraphmlData graphmlData : graphmlDatas) {
            graphmlWriter.data(graphmlData);
        }
        // emit other cjData as graphMl data
        writeData_Json(cjDoc);

        cjDoc.graphs().forEach(cjGraph -> {
            try {
                writeGraph(cjGraph);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        graphmlWriter.documentEnd();
    }

    public void writeEdge(ICjEdge cjEdge) throws IOException {
        List<ICjEndpoint> cjEndpointsList = cjEdge.endpoints().toList();
        List<ICjEndpoint> cjEps = new ArrayList<>(cjEndpointsList);
        Collections.sort(cjEps, Comparator.comparing(ICjEndpoint::node));
        Boolean directed = null;
        if (cjEps.size() == 2) {
            ICjEndpoint sourceEp = cjEps.get(0);
            ICjEndpoint targetEp = cjEps.get(1);
            if (sourceEp.isSource() && targetEp.isTarget()) {
                // good, directed simple bi-edge
                directed = true;
            } else if (sourceEp.isTarget() && targetEp.isSource()) {
                // ok, just need to switch EPs
                sourceEp = cjEps.get(1);
                targetEp = cjEps.get(0);
                directed = true;
            } else if (sourceEp.isUndirected() && targetEp.isUndirected()) {
                // ok, undirected simple bi-edge
                directed = false;
            }

            // else: edge can only be represented as hyper-edge
            if (directed != null) {
                GraphmlEdgeBuilder edgeBuilder = IGraphmlEdge.builder();
                edgeBuilder.id(cjEdge.id());
                edgeBuilder.directed(directed);
                edgeBuilder.sourceId(sourceEp.node());
                ifPresentAccept(sourceEp.port(), edgeBuilder::sourcePortId);
                edgeBuilder.targetId(targetEp.node());
                ifPresentAccept(targetEp.port(), edgeBuilder::targetPortId);

                writeData_Description(cjEdge, edgeBuilder);
                writeData_CustomAttributes(cjEdge, edgeBuilder);

                graphmlWriter.edgeStart(edgeBuilder.build());
            }
        }
        if (cjEps.size() != 2 || directed == null) {
            // default case: hyperedge
            GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder().id(cjEdge.id());
            for (ICjEndpoint cjEp : cjEps) {
                GraphmlEndpointBuilder graphmlEndpoint = IGraphmlEndpoint.builder();
                graphmlEndpoint.node(cjEp.node());
                GraphmlDirection gDir = GraphmlDirection.ofCj(cjEp.direction());
                graphmlEndpoint.type(gDir);
                ifPresentAccept(cjEp.port(), graphmlEndpoint::port);

                writeData_Description(cjEdge, builder);
                writeData_CustomAttributes(cjEdge, builder);

                builder.addEndpoint(graphmlEndpoint.build());
            }
            graphmlWriter.hyperEdgeStart(builder.build());
        }

        // CJ edge type encoded as Graphml:DATA
        ifPresentAccept(cjEdge.edgeType(), edgeType -> {
            try {
                String json = ICjEdgeType.toJsonString(edgeType);
                graphmlWriter.data(GraphmlDataElement.EdgeType.toGraphmlData(json));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writeCjLabelAsGraphmlData(cjEdge.label());
        writeData_Json(cjEdge);

        cjEdge.graphs().forEach(cjGraph -> {
            try {
                writeGraph(cjGraph);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (cjEdge.endpoints().count() == 2) {
            graphmlWriter.edgeEnd();
        } else {
            graphmlWriter.hyperEdgeEnd();
        }
    }

    public void writeGraph(ICjGraph cjGraph) throws IOException {
        GraphmlGraphBuilder graphmlBuilder = IGraphmlGraph.builder();

        ifPresentAccept(cjGraph.id(), graphmlBuilder::id);
        writeData_CustomAttributes(cjGraph, graphmlBuilder);
        new GraphmlParseInfo(GraphmlParseInfo.Ids.free, GraphmlParseInfo.Ids.free, GraphmlParseInfo.ParseOrder.nodesfirst, (int) cjGraph.countNodesDirect(), (int) cjGraph.countEdgesDirect()).toXmlAttributes(graphmlBuilder::attribute);

        writeData_Description(cjGraph, graphmlBuilder);
        graphmlBuilder.edgeDefault(IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT);
        graphmlWriter.graphStart(graphmlBuilder.build());

        writeCjLabelAsGraphmlData(cjGraph.label());

        // TODO GraphML extensions for graph stats (which exists)

        cjGraph.nodes().sorted(Comparator.comparing(ICjNode::id)).forEach(cjNode -> {
            try {
                writeNode(cjNode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        cjGraph.edges().forEach(cjEdge -> {
            try {
                writeEdge(cjEdge);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        cjGraph.graphs().forEach(cjSubGraph -> {
            try {
                // What to do when we are in a graph? GraphML has no graph-graph nesting.
                // We need to insert a synthetic node.
                graphmlWriter.nodeStart(IGraphmlNode.builder() //
                        .id("node-" + cjSubGraph.id()) //
                        .build());
                graphmlWriter.data(GraphmlDataElement.SyntheticNode.toGraphmlData("" + true));
                writeGraph(cjSubGraph);
                graphmlWriter.nodeEnd();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        graphmlWriter.graphEnd();
    }

    public void writeNode(ICjNode cjNode) throws IOException {
        GraphmlNodeBuilder graphmlBuilder = IGraphmlNode.builder();
        ifPresentAccept(cjNode.id(), graphmlBuilder::id);
        writeData_CustomAttributes(cjNode, graphmlBuilder);
        writeData_Description(cjNode, graphmlBuilder);
        graphmlWriter.nodeStart(graphmlBuilder.build());

        writeCjLabelAsGraphmlData(cjNode.label());
        writeData_Json(cjNode);

        // ports, graphs
        cjNode.ports().forEach(cjPort -> {
            try {
                writePort(cjPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        cjNode.graphs().forEach(cjGraph -> {
            try {
                writeGraph(cjGraph);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        graphmlWriter.nodeEnd();
    }

    public void writePort(ICjPort cjPort) throws IOException {
        GraphmlPortBuilder portBuilder = IGraphmlPort.builder();
        portBuilder.name(cjPort.id());
        writeData_CustomAttributes(cjPort, portBuilder);
        writeData_Description(cjPort, portBuilder);
        graphmlWriter.portStart(portBuilder.build());

        writeCjLabelAsGraphmlData(cjPort.label());
        writeData_Json(cjPort);

        cjPort.ports().forEach(subCjPort -> {
            try {
                writePort(subCjPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        graphmlWriter.portEnd();
    }

    private Stream<ICjData> findAllDatas(CjDocumentElement cjDoc) {
        return cjDoc.allElements().filter(e -> e instanceof ICjHasData).map(e -> ((ICjHasData) e).data()).filter(Objects::nonNull);
    }

/*
    private void indexKey(IGraphmlKey key) {
        Map<GraphmlKeyForType, IGraphmlKey> subMap = dataId_for_key.computeIfAbsent(key.id(), k -> new HashMap<>());
        IGraphmlKey prev = subMap.put(key.forType(), key);
        assert prev == null : "key already indexed: " + key;
    }
*/

    private void writeData_CustomAttributes(ICjHasData cjHasData, GraphmlElementBuilder<?> graphmlElement) {
        cjHasData.onDataValue(json -> {
            json.resolve(CjGraphmlMapping.CjDataProperty.CustomXmlAttributes.cjPropertyKey, xmlAttributes -> //
                    xmlAttributes.onProperties((k, v) -> graphmlElement.attribute(k, v.asString())));
        });
    }

    /** Write CJ .data.description to GraphMl {@code <desc>} in builder */
    private void writeData_Description(ICjHasData cjHasData, GraphmlElementWithDescBuilder<?> gHasDesc) {
        assert cjHasData != null;
        cjHasData.onDataValue(json -> {
            json.resolve(CjGraphmlMapping.CjDataProperty.Description.cjPropertyKey, desc -> //
                    gHasDesc.desc(IGraphmlDescription.builder().value(desc.asString()).build()));
        });
    }

    private IGraphmlKey findKey(String id) {
        return keys.stream().filter(graphmlKey -> graphmlKey.id_().equals(id)) //
                .findFirst().orElse(null);
    }

    /** Write CJ .data to GraphMl {@code <data>} */
    private void writeData_Json(ICjHasData cjHasData) throws IOException {
        ICjData data = cjHasData.data();
        if (data == null) return;
        IJsonValue value = data.jsonValue();
        if (value != null) {
            if(value.isPrimitive() || value.isArray()) {
                IGraphmlData graphmlData = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
                graphmlWriter.data(graphmlData);
                return;
            }
            // copy to new, mutable object
            JavaJsonObject mutableObject = JavaJsonObject.copyOf(value.asObject());
            mutableObject.removePropertyIf( key -> key.startsWith("graphml:"));

            // decide how to express this data in GraphML
            if (mapsToIndividualGraphmlProperties(mutableObject)) {
                // write as individual properties
                mutableObject.forEach((key, val) -> //
                {
                    IGraphmlKey graphmlKey = findKey(key);
                    assert graphmlKey != null : "no key found for " + key + " in " + keys;
                    IGraphmlData graphmlData = toGraphmlData(graphmlKey, val);
                    try {
                        graphmlWriter.data(graphmlData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                IGraphmlData graphmlData = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
                graphmlWriter.data(graphmlData);
            }
        }
    }

    private IGraphmlData toGraphmlData(IGraphmlKey key, IJsonValue jsonValue) {
        if(IJsonTypedString.isTypedString(jsonValue)) {
            // special
            IJsonTypedString typedString = IJsonTypedString.asJsonTypedString(jsonValue);
            if(typedString.type().equals(IJsonTypedString.TYPE_XML)) {
                String xml = typedString.value();
                return IGraphmlData.ofRawXml(key.id(), xml);
            } else {
                throw new IllegalArgumentException("Unknown type for JsonTypedString '" + typedString.type() +
                        "'");
            }
        } else if (jsonValue.isPrimitive()) {
            return key.toGraphmlData(""+jsonValue.asPrimitive().base());
        } else {
            return key.toGraphmlData(jsonValue.toJsonString());
        }
    }


}
