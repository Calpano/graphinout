package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjHasData;
import com.calpano.graphinout.base.cj.element.ICjLabel;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
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
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;

/**
 * CJ to GraphML: {@link ICjDocument} to {@link GraphmlWriter}.
 */
public class CjDocument2Graphml {

    private final GraphmlWriter graphmlWriter;

    /** data@id -> key@for -> <key> */
    private final Map<String, Map<GraphmlKeyForType, IGraphmlKey>> dataId_for_key = new HashMap<>();

    public CjDocument2Graphml(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    public static void writeToGraphml(ICjDocument cjDoc, GraphmlWriter graphmlWriter) throws IOException {
        assert cjDoc != null;
        new CjDocument2Graphml(graphmlWriter).writeDocumentToGraphml(cjDoc);
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

    public void writeCjLabelAsGraphmlData(@Nullable ICjLabel cjLabel) throws IOException {
        if (cjLabel == null) {
            return;
        }
        this.graphmlWriter.data(GraphmlDataElement.Label.toGraphmlData(cjLabel.toJsonString()));
    }

    /** Write given document to GraphML */
    public void writeDocumentToGraphml(ICjDocument cjDoc) throws IOException {
        assert cjDoc != null;
        GraphmlDocumentBuilder graphmlBuilder = IGraphmlDocument.builder();
        // map _SOME_ cjData to native graphMl constructs
        writeData_Description(cjDoc, graphmlBuilder);
        write_CustomAttributes(cjDoc, graphmlBuilder);

        // define which data types are used in this document
        List<IGraphmlKey> keys = new ArrayList<>();
        if (cjDoc.baseUri() != null) {
            keys.add(GraphmlDataElement.BaseUri.toGraphmlKey());
        }
        keys.add(GraphmlDataElement.EdgeTypeSource.toGraphmlKey());
        keys.add(GraphmlDataElement.EdgeTypeValue.toGraphmlKey());
        keys.add(GraphmlDataElement.Label.toGraphmlKey());
        keys.add(GraphmlDataElement.SyntheticNode.toGraphmlKey());
        // prepare <key> for CJ:data (graphml needs it pre-declared)
        keys.add(GraphmlDataElement.CjJsonData.toGraphmlKey());

        // prepare <key> for CJ:baseUri (graphml has no baseUri)
        List<IGraphmlData> graphmlDatas = new ArrayList<>();
        ifPresentAccept(cjDoc.baseUri(), baseUri -> //
                graphmlDatas.add(IGraphmlData.ofPlainString(GraphmlDataElement.BaseUri.name(), baseUri)));

        // <!ELEMENT graphml  (desc?,key*,(data|graph)*)>
        graphmlWriter.documentStart(graphmlBuilder.build());
        for (IGraphmlKey key : keys) {
            graphmlWriter.key(key);
        }

        // emit other cjData as graphMl data
        toGraphmlData(cjDoc, graphmlDatas::add);
        // emit graphml document level data
        for (IGraphmlData graphmlData : graphmlDatas) {
            graphmlWriter.data(graphmlData);
        }

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
        List<ICjEndpoint> cjEps = new ArrayList<>(cjEdge.endpoints().toList());
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
                write_CustomAttributes(cjEdge, edgeBuilder);

                graphmlWriter.edgeStart(edgeBuilder.build());
            }
        }
        if (cjEps.size() != 2 || directed == null) {
            // default case: hyperedge
            GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder().id(cjEdge.id());
            graphmlWriter.hyperEdgeStart(builder.build());
            // FIXME all endpoints
            for (ICjEndpoint cjEp : cjEps) {
                GraphmlEndpointBuilder graphmlEndpoint = IGraphmlEndpoint.builder();
                graphmlEndpoint.node(cjEp.node());
                GraphmlDirection gDir = GraphmlDirection.ofCj(cjEp.direction());
                graphmlEndpoint.type(gDir);
                ifPresentAccept(cjEp.port(), graphmlEndpoint::port);

                writeData_Description(cjEdge, builder);
                write_CustomAttributes(cjEdge, builder);

                builder.addEndpoint(graphmlEndpoint.build());
            }
        }

        // CJ edge type encoded as Graphml:DATA
        ifPresentAccept(cjEdge.edgeType(), edgeType -> {
            try {
                graphmlWriter.data(GraphmlDataElement.EdgeTypeSource.toGraphmlData(edgeType.source().name()));
                graphmlWriter.data(GraphmlDataElement.EdgeTypeValue.toGraphmlData(edgeType.type()));
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
        write_CustomAttributes(cjGraph, graphmlBuilder);
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
        write_CustomAttributes(cjNode, graphmlBuilder);
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
        write_CustomAttributes(cjPort, portBuilder);
        writeData_Description(cjPort, portBuilder);
        graphmlWriter.portStart(portBuilder.build());
        cjPort.ports().forEach(subCjPort -> {
            try {
                writePort(subCjPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        graphmlWriter.portEnd();
    }

    private void toGraphmlData(ICjHasData cjHasData, Consumer<IGraphmlData> graphmlDataConsumer) {
        cjHasData.onDataValue(json -> graphmlDataConsumer.accept( //
                GraphmlDataElement.CjJsonData.toGraphmlData(json.toJsonString()) //
        ));
    }

/*
    private void indexKey(IGraphmlKey key) {
        Map<GraphmlKeyForType, IGraphmlKey> subMap = dataId_for_key.computeIfAbsent(key.id(), k -> new HashMap<>());
        IGraphmlKey prev = subMap.put(key.forType(), key);
        assert prev == null : "key already indexed: " + key;
    }
*/

    private void writeData_Description(ICjHasData cjHasData, GraphmlElementWithDescBuilder<?> gHasDesc) {
        assert cjHasData != null;
        cjHasData.onDataValue(json -> {
            json.resolve(CjGraphmlMapping.CjDataProperty.Description.cjPropertyKey, desc -> //
                    gHasDesc.desc(IGraphmlDescription.builder().value(desc.asString()).build()));
        });
    }

    private void writeData_Json(ICjHasData cjHasData) throws IOException {
        ICjData data = cjHasData.data();
        if (data == null) return;
        IJsonValue value = data.jsonValue();
        if (value != null) {
            IGraphmlData gd = GraphmlDataElement.CjJsonData.toGraphmlData(value.toJsonString());
            graphmlWriter.data(gd);
        }
    }

    private void write_CustomAttributes(ICjHasData cjHasData, GraphmlElementBuilder<?> graphmlElement) {
        cjHasData.onDataValue(json -> {
            json.resolve(CjGraphmlMapping.CjDataProperty.CustomXmlAttributes.cjPropertyKey, xmlAttributes -> //
                    xmlAttributes.onProperties((k, v) -> graphmlElement.attribute(k, v.asString())));
        });
    }


}
