package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjEdge;
import com.graphinout.base.cj.element.ICjEndpoint;
import com.graphinout.base.cj.element.ICjGraph;
import com.graphinout.base.cj.element.ICjHasData;
import com.graphinout.base.cj.element.ICjLabel;
import com.graphinout.base.cj.element.ICjNode;
import com.graphinout.base.cj.element.ICjPort;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.graphinout.base.graphml.GraphmlDirection;
import com.graphinout.base.graphml.GraphmlKeyForType;
import com.graphinout.base.graphml.GraphmlParseInfo;
import com.graphinout.base.graphml.GraphmlWriter;
import com.graphinout.base.graphml.IGraphmlData;
import com.graphinout.base.graphml.IGraphmlDefault;
import com.graphinout.base.graphml.IGraphmlDescription;
import com.graphinout.base.graphml.IGraphmlDocument;
import com.graphinout.base.graphml.IGraphmlEdge;
import com.graphinout.base.graphml.IGraphmlEndpoint;
import com.graphinout.base.graphml.IGraphmlGraph;
import com.graphinout.base.graphml.IGraphmlHyperEdge;
import com.graphinout.base.graphml.IGraphmlKey;
import com.graphinout.base.graphml.IGraphmlNode;
import com.graphinout.base.graphml.IGraphmlPort;
import com.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.graphinout.base.graphml.builder.GraphmlEdgeBuilder;
import com.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.graphinout.base.graphml.builder.GraphmlElementWithDescBuilder;
import com.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.graphinout.base.graphml.impl.GraphmlData;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonObject;
import com.graphinout.foundation.util.Nullables;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.graphinout.foundation.util.PowerStreams.forEach;

/**
 * CJ to GraphML: {@link ICjDocument} to {@link GraphmlWriter}.
 */
public class CjDocument2Graphml {

    private final GraphmlWriter graphmlWriter;
    private GraphmlSchema graphmlSchema;

    public CjDocument2Graphml(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    /** Synthetic nodes allow graphml to represent, e.g., a CJ's graph-graph nesting */
    public static boolean containsSyntheticNodes(ICjDocument cjDoc) {
        return CjData2GraphmlKeyData.findAllDatas((CjDocumentElement) cjDoc) //
                .map(ICjData::jsonValue).filter(Objects::nonNull) //
                .filter(IJsonValue::isObject).map(IJsonValue::asObject) //
                .anyMatch(o -> o.hasProperty(CjGraphmlMapping.CjDataProperty.SyntheticNode.cjPropertyKey));
    }

    public static void writeToGraphml(ICjDocument cjDoc, GraphmlWriter graphmlWriter) throws IOException {
        assert cjDoc != null;
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
        writeData_Json(cjDoc);

        forEach(cjDoc.graphs(), this::writeGraph);

        graphmlWriter.documentEnd();
    }

    public void writeEdge(ICjEdge cjEdge) throws IOException {
        List<ICjEndpoint> cjEndpointsList = cjEdge.endpoints().toList();
        List<ICjEndpoint> cjEps = new ArrayList<>(cjEndpointsList);
        cjEps.sort(Comparator.comparing(ICjEndpoint::node));
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

        forEach(cjEdge.graphs(), this::writeGraph);

        if (cjEdge.endpoints().count() == 2) {
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
        writeData_Json(cjGraph);

        writeCjLabelAsGraphmlData(cjGraph.label());


        forEach(cjGraph.nodes().sorted(Comparator.comparing(node -> Nullables.nonNullOrEmpty(node.id()))), this::writeNode);
        forEach(cjGraph.edges().sorted(Comparator.comparing(edge -> Nullables.nonNullOrEmpty(edge.id()))), this::writeEdge);

        forEach(cjGraph.graphs(), cjSubGraph -> {
            // What to do when we are in a graph? GraphML has no graph-graph nesting.
            // We need to insert a synthetic node.
            graphmlWriter.nodeStart(IGraphmlNode.builder() //
                    .id("node-" + cjSubGraph.id()) //
                    .build());
            graphmlWriter.data(GraphmlDataElement.SyntheticNode.toGraphmlData("" + true));
            writeGraph(cjSubGraph);
            graphmlWriter.nodeEnd();
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
        writeData_Json(cjPort);

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


    private void writeData_CustomAttributes(ICjHasData cjHasData, GraphmlElementBuilder<?> graphmlElement) {
        cjHasData.onDataValue(json -> //
                json.resolve(CjGraphmlMapping.CjDataProperty.CustomXmlAttributes.cjPropertyKey, xmlAttributes -> //
                        xmlAttributes.onProperties((k, v) -> graphmlElement.attribute(k, v.asString()))));
    }

    /** Write CJ .data.description to GraphMl {@code <desc>} in builder */
    private void writeData_Description(ICjHasData cjHasData, GraphmlElementWithDescBuilder<?> gHasDesc) {
        assert cjHasData != null;
        cjHasData.onDataValue(json -> //
                json.resolve(CjGraphmlMapping.CjDataProperty.Description.cjPropertyKey, desc -> //
                        gHasDesc.desc(IGraphmlDescription.of(desc.toXmlFragmentString()))));
    }

    /** Write CJ .data to GraphMl {@code <data>} */
    private void writeData_Json(ICjHasData cjHasData) throws IOException {
        ICjData data = cjHasData.data();
        if (data == null) return;
        IJsonValue value = data.jsonValue();
        if (value == null) return;

        if (value.isPrimitive() || value.isArray()) {
            IGraphmlData graphmlData = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
            graphmlWriter.data(graphmlData);
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
                IGraphmlKey graphmlKey = graphmlSchema.findKeyByForAndAttrName(graphmlKeyForType(cjHasData.cjType()), propertyKey);
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
