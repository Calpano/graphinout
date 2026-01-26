package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.data.CjMappedProperties;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjPort;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.functional.ThrowingConsumer;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonObject;
import com.graphinout.foundation.pure.stream.PowerStreams;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import com.graphinout.reader.graphml.IGraphmlWriter;
import com.graphinout.reader.graphml.cj.CjGraphmlMapping.GraphmlDataElement;
import com.graphinout.reader.graphml.elements.GraphmlDirection;
import com.graphinout.reader.graphml.elements.GraphmlKeyForType;
import com.graphinout.reader.graphml.elements.GraphmlParseInfo;
import com.graphinout.reader.graphml.elements.IGraphmlData;
import com.graphinout.reader.graphml.elements.IGraphmlDefault;
import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlDocument;
import com.graphinout.reader.graphml.elements.IGraphmlEndpoint;
import com.graphinout.reader.graphml.elements.IGraphmlGraph;
import com.graphinout.reader.graphml.elements.IGraphmlKey;
import com.graphinout.reader.graphml.elements.IGraphmlNode;
import com.graphinout.reader.graphml.elements.IGraphmlPort;
import com.graphinout.reader.graphml.elements.builder.GraphmlDataBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlDocumentBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlEdgeBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlElementBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlElementWithDescBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlEndpointBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlGraphBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlHyperEdgeBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlNodeBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlPortBuilder;
import com.graphinout.reader.graphml.elements.impl.GraphmlData;
import com.graphinout.reader.graphml.elements.impl.GraphmlEdge;
import com.graphinout.reader.graphml.elements.impl.GraphmlEndpoint;
import com.graphinout.reader.graphml.elements.impl.GraphmlHyperEdge;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.stream.PowerStreams.forEach;

/**
 * CJ to GraphML: {@link ICjDocument} to {@link IGraphmlWriter}.
 */
public class CjDocument2Graphml {

    private final IGraphmlWriter graphmlWriter;
    private GraphmlSchema graphmlSchema;

    public CjDocument2Graphml(IGraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    /**
     * Synthetic nodes allow graphml to represent, e.g., a CJ's graph-graph nesting.
     */
    public static boolean containsSyntheticNodes(ICjDocument cjDoc) {
        return CjData2GraphmlKeyData.findAllDatas((CjDocumentElement) cjDoc) //
                .map(ICjData::jsonValue).filter(Objects::nonNull) //
                .filter(IJsonValue::isObject).map(IJsonValue::asObject) //
                .anyMatch(o -> o.hasProperty(CjDataProperty.SyntheticNode.cjPropertyKey));
    }

    /** @return true iff there is at least one graph-graph nesting */
    public static boolean requiresSyntheticNodes(ICjDocument cjDoc) {
        return cjDoc.allElements().anyMatch(cjElement -> cjElement.cjType() == CjType.Graph && cjElement.directChildren().anyMatch(child -> child.cjType() == CjType.Graph));
    }

    /**
     *
     * @param cjDoc         is modified in-place to remove synthetic nodes if they ar present. So graph-node-graph is
     *                      changed to graph-graph nesting.
     * @param graphmlWriter
     * @throws IOException
     */
    public static void writeToGraphml(ICjDocument cjDoc, IGraphmlWriter graphmlWriter) throws IOException {
        if (cjDoc == null) return;
        assert !containsSyntheticNodes(cjDoc) : " post-process was not called";
        new CjDocument2Graphml(graphmlWriter).writeDocumentToGraphml(cjDoc);
    }

    /**
     * Expects the {@link #graphmlSchema} to contain an entry for {@link GraphmlDataElement#Label}
     *
     * @param cjLabel optional
     */
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
        XmlFragmentString xmlFragmentString = XmlFragmentString.ofPlainText(value);
        this.graphmlWriter.data(GraphmlDataElement.Label.toGraphmlData(xmlFragmentString));
    }

    /** Write given document to GraphML */
    public void writeDocumentToGraphml(ICjDocument cjDoc) throws IOException {
        assert cjDoc != null;
        GraphmlDocumentBuilder graphmlBuilder = IGraphmlDocument.builder();
        // map _SOME_ cjData to native graphMl constructs
        writeData_Description(cjDoc, graphmlBuilder);
        writeData_CustomAttributes(cjDoc, graphmlBuilder);

        this.graphmlSchema = CjData2GraphmlKeyData.buildGraphmlSchema(cjDoc);

        boolean usesCjData = PowerStreams.filterMap(cjDoc.allElements(), ICjHasData.class) //
                .map(ICjHasData::dataIfNotEmpty).anyMatch(Objects::nonNull);
        if (!usesCjData) {
            graphmlSchema.removeKeyById(CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName);
        }

        // TODO are cj_jsonData used in this document?
        // TODO are cj_label used in this doc?
        // TODO are cj_edgeType used?

        // FIXME filter out unused keys (such as some builtin CJ)

        // <!ELEMENT graphml  (desc?,key*,(data|graph)*)>
        graphmlWriter.documentStart(graphmlBuilder.build());

        forEach(graphmlSchema.keys(), graphmlWriter::key);

        // Write <data> for CJ:baseUri (Graphml has no baseUri)
        List<IGraphmlData> graphmlDatas = new ArrayList<>();
        ifPresentAccept(cjDoc.baseUri(), baseUri -> //
                graphmlDatas.add(GraphmlDataElement.BaseUri.toGraphmlData(baseUri)));

        // emit Graphml document level data
        for (IGraphmlData graphmlData : graphmlDatas) {
            graphmlWriter.data(graphmlData);
        }
        // emit cjData as graphMl data
        writeData_Json(cjDoc, graphmlWriter::data);

        forEach(cjDoc.graphs(), this::writeGraph);

        graphmlWriter.documentEnd();
    }

    public void writeEdge(ICjEdge cjEdge) throws IOException {
        List<ICjEndpoint> cjEps = new ArrayList<>(cjEdge.endpoints().toList());
        cjEps.sort(Comparator.comparing(ICjEndpoint::node));
        boolean useSimpleEdge;
        if (cjEps.size() == 2) {
            ICjEndpoint sourceEp = cjEps.get(0);
            ICjEndpoint targetEp = cjEps.get(1);
            if (sourceEp.data().isNotEmpty() || targetEp.data().isNotEmpty()) {
                useSimpleEdge = false;
            } else if (sourceEp.isSource() && targetEp.isTarget()) {
                // good, directed simple bi-edge
                useSimpleEdge = true;
                GraphmlEdge biEdge = startBiEdge(cjEdge, sourceEp, targetEp, true);
                graphmlWriter.edgeStart(biEdge);
            } else if (sourceEp.isTarget() && targetEp.isSource()) {
                // ok, just need to switch EPs
                sourceEp = cjEps.get(1);
                targetEp = cjEps.get(0);
                useSimpleEdge = true;
                GraphmlEdge biEdge = startBiEdge(cjEdge, sourceEp, targetEp, true);
                graphmlWriter.edgeStart(biEdge);
            } else if (sourceEp.isUndirected() && targetEp.isUndirected()) {
                // ok, undirected simple bi-edge
                useSimpleEdge = true;
                GraphmlEdge biEdge = startBiEdge(cjEdge, sourceEp, targetEp, false);
                graphmlWriter.edgeStart(biEdge);
            } else {
                useSimpleEdge = false;
            }
        } else {
            useSimpleEdge = false;
        }
        if (!useSimpleEdge) {
            // else: edge can only be represented as hyper-edge
            // default case: hyperedge
            GraphmlHyperEdge hyperEdge = startHyperEdge(cjEdge, cjEps);
            graphmlWriter.hyperEdgeStart(hyperEdge);
        }
        // == edge has been started, next elements are inside edge

        // CJ edge type encoded as Graphml:DATA
        ifPresentAccept(cjEdge.edgeType(), edgeType -> {
            try {
                String json = ICjElementType.toJsonString(edgeType);
                graphmlWriter.data(GraphmlDataElement.EdgeType.toGraphmlData(json));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writeCjLabelAsGraphmlData(cjEdge.label());
        writeData_Json(cjEdge, graphmlWriter::data);

        forEach(cjEdge.graphs(), this::writeGraph);

        if (useSimpleEdge) {
            graphmlWriter.edgeEnd();
        } else {
            graphmlWriter.hyperEdgeEnd();
        }
    }

    public void writeGraph(ICjGraph cjGraph) throws IOException {
        GraphmlGraphBuilder graphmlBuilder = IGraphmlGraph.builder();

        // == Attributes
        ifPresentAccept(cjGraph.id(), graphmlBuilder::id);
        graphmlBuilder.edgeDefault(IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT);
        writeData_CustomAttributes(cjGraph, graphmlBuilder);
        // GraphML extensions for graph stats -- "parse.info"
        new GraphmlParseInfo(GraphmlParseInfo.Ids.free, GraphmlParseInfo.Ids.free, GraphmlParseInfo.ParseOrder.nodesfirst, (int) cjGraph.countNodesDirect(), (int) cjGraph.countEdgesDirect()).toXmlAttributes(graphmlBuilder::attribute);

        // == Child Elements
        writeData_Description(cjGraph, graphmlBuilder);

        graphmlWriter.graphStart(graphmlBuilder.build());

        // Write graph-level baseUri as GraphML data (CJ 7.0.0)
        ifPresentAccept(cjGraph.baseUri(), baseUri -> {
            try {
                graphmlWriter.data(GraphmlDataElement.GraphBaseUri.toGraphmlData(baseUri));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writeData_Json(cjGraph, graphmlWriter::data);

        writeCjLabelAsGraphmlData(cjGraph.label());


        forEach(cjGraph.nodes().sorted(Comparator.comparing(node -> Nullables.nonNullOrEmpty(node.id()))), this::writeNode);
        forEach(cjGraph.edges().sorted(Comparator.comparing(edge -> Nullables.nonNullOrEmpty(edge.id()))), this::writeEdge);

        forEach(cjGraph.graphs(), cjSubGraph -> {
            // What to do when we are in a graph? GraphML has no graph-graph nesting.
            // We need to insert a synthetic node.
            graphmlWriter.nodeStart(IGraphmlNode.builder() //
                    // create a unique subgraph id, IMPROVE use a UUID?
                    .id("node-" + cjSubGraph.id()) //
                    .build());
            graphmlWriter.data(GraphmlDataElement.SyntheticNode.toGraphmlData("" + true));
            writeGraph(cjSubGraph);
            graphmlWriter.nodeEnd();
        });

        graphmlWriter.graphEnd();
    }

    public void writeNode(ICjNode cjNode) throws IOException {
        GraphmlNodeBuilder nodeBuilder = IGraphmlNode.builder();
        ifPresentAccept(cjNode.uri(), nodeBuilder::id);
        writeData_CustomAttributes(cjNode, nodeBuilder);
        writeData_Description(cjNode, nodeBuilder);
        graphmlWriter.nodeStart(nodeBuilder.build());

        writeCjLabelAsGraphmlData(cjNode.label());

        // Write node types as GraphML data (JSON array)
        List<ICjElementType> nodeTypes = cjNode.types().toList();
        if (!nodeTypes.isEmpty()) {
            try {
                // Manually construct JSON array: ["type1", "type2", ...]
                StringBuilder jsonArray = new StringBuilder("[");
                for (int i = 0; i < nodeTypes.size(); i++) {
                    if (i > 0) jsonArray.append(",");
                    jsonArray.append(ICjElementType.toJsonString(nodeTypes.get(i)));
                }
                jsonArray.append("]");
                graphmlWriter.data(GraphmlDataElement.NodeTypes.toGraphmlData(jsonArray.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        writeData_Json(cjNode, graphmlWriter::data);

        forEach(cjNode.ports(), this::writePort);
        forEach(cjNode.graphs(), this::writeGraph);

        graphmlWriter.nodeEnd();
    }

    public void writePort(ICjPort cjPort) throws IOException {
        GraphmlPortBuilder portBuilder = IGraphmlPort.builder();
        portBuilder.name(cjPort.id());
        writeData_CustomAttributes(cjPort, portBuilder);
        writeData_Description(cjPort, portBuilder);
        graphmlWriter.portStart(portBuilder.build());

        writeCjLabelAsGraphmlData(cjPort.label());
        writeData_Json(cjPort, graphmlWriter::data);

        forEach(cjPort.ports(), this::writePort);
        graphmlWriter.portEnd();
    }

    private GraphmlKeyForType graphmlKeyForType(CjType cjType) {
        return switch (cjType) {
            case RootObject -> GraphmlKeyForType.Graphml;
            case Graph -> GraphmlKeyForType.Graph;
            case Node -> GraphmlKeyForType.Node;
            case Port -> GraphmlKeyForType.Port;
            case Edge -> GraphmlKeyForType.Edge;
            case Endpoint -> GraphmlKeyForType.Endpoint;
            default -> throw new IllegalStateException("Unexpected value: " + cjType);
        };
    }

    private GraphmlEdge startBiEdge(ICjEdge cjEdge, ICjEndpoint sourceEp, ICjEndpoint targetEp, boolean directed) {
        GraphmlEdgeBuilder edgeBuilder = new GraphmlEdgeBuilder();
        ifPresentAccept(cjEdge.id(), edgeBuilder::id);
        writeData_Description(cjEdge, edgeBuilder);
        writeData_CustomAttributes(cjEdge, edgeBuilder);

        edgeBuilder.directed(directed);
        edgeBuilder.sourceId(cjEdge.resolveNodeById(sourceEp.node()).abbreviatedUri());
        ifPresentAccept(sourceEp.port(), edgeBuilder::sourcePortId);
        edgeBuilder.targetId(cjEdge.resolveNodeById(targetEp.node()).abbreviatedUri());
        ifPresentAccept(targetEp.port(), edgeBuilder::targetPortId);

        return edgeBuilder.build();
    }

    private GraphmlHyperEdge startHyperEdge(ICjEdge cjEdge, List<ICjEndpoint> cjEps) {
        GraphmlHyperEdgeBuilder hyperEdgeBuilder = new GraphmlHyperEdgeBuilder();
        ifPresentAccept(cjEdge.id(), hyperEdgeBuilder::id);

        writeData_CustomAttributes(cjEdge, hyperEdgeBuilder);

        writeData_Description(cjEdge, hyperEdgeBuilder);

        for (ICjEndpoint cjEp : cjEps) {
            GraphmlEndpoint graphmlEndpoint = toGraphmlEndpoint(cjEdge, cjEp);
            hyperEdgeBuilder.addEndpoint(graphmlEndpoint);
        }
        return hyperEdgeBuilder.build();
    }

    /**
     * @param key       for key id and preferred graphml type
     * @param jsonValue to use as data
     * @return GraphML data
     */
    private GraphmlData toGraphmlData(IGraphmlKey key, IJsonValue jsonValue) {
        XmlFragmentString xmlFragmentString = CjGraphmlMapping.toXmlFragment(key.attrTypeAsGraphmlDataType(), jsonValue);
        GraphmlDataBuilder builder = IGraphmlData.builder();
        builder.xmlValue(xmlFragmentString);
        builder.key(key.id_());
        return builder.build();
    }

    private GraphmlEndpoint toGraphmlEndpoint(ICjEdge cjEdge, ICjEndpoint cjEp) {
        GraphmlEndpointBuilder graphmlEndpoint = IGraphmlEndpoint.builder();

        ICjNode resolvedNode = cjEdge.resolveNodeById(cjEp.node());
        graphmlEndpoint.node(resolvedNode.abbreviatedUri());

        GraphmlDirection gDir = GraphmlDirection.ofCj(cjEp.direction());
        graphmlEndpoint.type(gDir);

        ifPresentAccept(cjEp.port(), graphmlEndpoint::port);

        // endpoint type (not confuse with dir)
        ifPresentAccept(cjEp.type(), cjType -> {
            graphmlEndpoint.attribute(CjMappedProperties.CJ_ENDPOINT_TYPE, cjType);
        });

        writeData_Description(cjEp, graphmlEndpoint);
        writeData_CustomAttributes(cjEp, graphmlEndpoint);

        // NOTE: GraphML XSD 1.1 has a bug: <endpoints> may not have <data>, despite comments in same spec
        // saying otherwise. Also <key> is allowed on <endpoint>.
        cjEp.data(data -> {
            IJsonValue jsonValue = data.jsonValue();
            if (jsonValue == null)
                return;
            if (jsonValue.isObject()) {
                IJsonObjectMutable o = jsonValue.asObject().mutableCopy();
                o.removeProperty(CjDataProperty.CustomXmlAttributes.cjPropertyKey);
                jsonValue = o;
            }
            // put remaining data in a single XML attribute
            graphmlEndpoint.attribute(CjMappedProperties.CJ_DATA, jsonValue.toJsonString());
        });
        return graphmlEndpoint.build();
    }

    private void writeData_CustomAttributes(ICjHasData cjHasData, GraphmlElementBuilder<?> graphmlElement) {
        cjHasData.onDataValue(json -> {
            // Only objects can have properties to resolve
            if (!json.isObject()) return;
            json.resolve(CjDataProperty.CustomXmlAttributes.cjPropertyKey, xmlAttributes -> //
                    xmlAttributes.onProperties((k, v) -> graphmlElement.attribute(k, v.asString())));
        });
    }

    /** Write CJ .data.description to GraphMl {@code <desc>} in builder */
    private void writeData_Description(ICjHasData cjHasData, GraphmlElementWithDescBuilder<?> gHasDesc) {
        assert cjHasData != null;
        cjHasData.onDataValue(json -> {
            // Only objects can have properties to resolve
            if (!json.isObject()) return;
            json.resolve(CjDataProperty.Description.cjPropertyKey, desc -> //
                    gHasDesc.desc(IGraphmlDescription.of(desc.toXmlFragmentString())));
        });
    }

    /** Write CJ .data to GraphMl {@code <data>} */
    private void writeData_Json(ICjHasData cjHasData, ThrowingConsumer<IGraphmlData, IOException> graphmlDataConsumer) throws IOException {
        IJsonValue value = cjHasData.data().jsonValue();
        if (value == null) return;

        if (value.isPrimitive() || value.isArray()) {
            IGraphmlData graphmlData = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
            graphmlDataConsumer.accept(graphmlData);
            return;
        }

        // copy to new, mutable object
        JavaJsonObject mutableObject = JavaJsonObject.copyOf(value.asObject());

        mutableObject.removePropertyIf(key -> key.startsWith("graphml:"));

        // decide how to express this data in GraphML
        if (CjData2GraphmlKeyData.mapsToIndividualGraphmlProperties(mutableObject)) {
            // write as individual properties
            mutableObject.forEach((propertyKey, val) -> //
            {
                IGraphmlKey graphmlKey = graphmlSchema.findKeyByForAndAttrName(graphmlKeyForType(cjHasData.asCjElement().cjType()), propertyKey);
                assert graphmlKey != null : "no key found for " + propertyKey + " in " + graphmlSchema;
                GraphmlData graphmlData = toGraphmlData(graphmlKey, val);

                //  avoid writing data which is identical to default value defined in KEY
                IGraphmlDefault defaultValue = graphmlKey.defaultValue();
                XmlFragmentString xmlFragmentString = graphmlData.xmlValue();
                if (defaultValue != null && defaultValue.xmlValue().equals(xmlFragmentString)) {
                    return;
                }

                if (xmlFragmentString != null && xmlFragmentString.xmlSpace() == XML.XmlSpace.preserve) {
                    graphmlData.addXmlAttributes(Map.of(XML.XML_SPACE, XML.XML_SPACE__PRESERVE));
                }

                try {
                    graphmlDataConsumer.accept(graphmlData);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            IGraphmlData graphmlData = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
            graphmlDataConsumer.accept(graphmlData);
        }
    }

}
