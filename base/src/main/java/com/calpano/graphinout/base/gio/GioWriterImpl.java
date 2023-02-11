package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.Direction;
import com.calpano.graphinout.base.graphml.GraphmlData;
import com.calpano.graphinout.base.graphml.GraphmlDefault;
import com.calpano.graphinout.base.graphml.GraphmlDescription;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlElement;
import com.calpano.graphinout.base.graphml.GraphmlEndpoint;
import com.calpano.graphinout.base.graphml.GraphmlGraph;
import com.calpano.graphinout.base.graphml.GraphmlGraphCommonElement;
import com.calpano.graphinout.base.graphml.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.GraphmlKey;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlLocator;
import com.calpano.graphinout.base.graphml.GraphmlNode;
import com.calpano.graphinout.base.graphml.GraphmlPort;
import com.calpano.graphinout.base.graphml.GraphmlWriter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GioWriterImpl implements GioWriter {

    final GraphmlWriter graphmlWriter;

    public GioWriterImpl(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void endDocument() throws IOException {
        graphmlWriter.endDocument();
    }

    @Override
    public void endEdge(Optional<URL> locator) throws IOException {
        graphmlWriter.endHyperEdge(locator(locator));
    }

    @Override
    public void endGraph(Optional<URL> locator) throws IOException {
        graphmlWriter.endGraph(locator(locator));
    }

    @Override
    public void endNode(Optional<URL> locator) throws IOException {
        graphmlWriter.endNode(locator(locator));

    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        GraphmlKey graphmlKey = GraphmlKey.builder()//
                .id(gioKey.getId())//
                .forType(GraphmlKeyForType.valueOf(gioKey.getForType().name()))//
                .build();
        gioKey.getDefaultValue().ifPresent(defaultValue -> graphmlKey.setDefaultValue(GraphmlDefault.builder().value(defaultValue).build()));
        gioKey.getAttributeName().ifPresent(graphmlKey::setAttrName);
        gioKey.getAttributeType().ifPresent(attType -> graphmlKey.setAttrType(attType.graphmlName));
        customAttributes(gioKey, graphmlKey);
        desc(gioKey, graphmlKey);
        graphmlWriter.data(graphmlKey);
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        GraphmlDocument graphmlDocument = new GraphmlDocument();
        desc(document, graphmlDocument);
        if (document.getKeys() != null) {
            for (GioKey gioKey : document.getKeys()) {
                key(gioKey);
            }
        }
        if (document.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            graphmlDocument.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlDocument.setDataList(graphmlDataList);
        }
        graphmlWriter.startDocument(graphmlDocument);
    }

    @Override
    public void startEdge(GioEdge gioEdge) throws IOException {
        GraphmlHyperEdge graphmlHyperEdge = GraphmlHyperEdge.builder().id(gioEdge.getId()).build();
        customAttributes(gioEdge, graphmlHyperEdge);
        data(gioEdge, graphmlHyperEdge);
        assert gioEdge.getEndpoints() != null;
        graphmlHyperEdge.setEndpoints(gioEdge.getEndpoints().stream().map(this::graphmlEndpoint).collect(Collectors.toList()));
        graphmlWriter.startHyperEdge(graphmlHyperEdge);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        GraphmlGraph graphmlGraph = GraphmlGraph.builder().id(gioGraph.getId()).edgedefault(gioGraph.isEdgedefault()).build();
        desc(gioGraph, graphmlGraph);
        data(gioGraph, graphmlGraph);

        graphmlWriter.startGraph(graphmlGraph);
        // TODO instead, remember all nodes ids of the stream, also node ids refered to in edges -> ValidatingGioWriter
//        if (gioGraph.getNodes() != null) {
//            for (GioNode gioNode : gioGraph.getNodes())
//                startNode(gioNode);
//        }
//        if (gioGraph.getHyperEdges() != null) {
//            for (GioEdge gioEdge : gioGraph.getHyperEdges())
//                startEdge(gioEdge);
//        }

    }

    @Override
    public void startNode(GioNode gioNode) throws IOException {
        GraphmlNode graphmlNode = GraphmlNode.builder().id(gioNode.getId()).build();
        desc(gioNode, graphmlNode);
        data(gioNode, graphmlNode);
        graphmlNode.setPorts(gioNode.getPorts().stream().map(this::port).collect(Collectors.toList()));
        graphmlWriter.startNode(graphmlNode);
    }

    private void customAttributes(GioElement gioElement, GraphmlElement graphmlElement) {
        // TODO validate in GraphmlWriter we dont overwrite the already defined attributes
        graphmlElement.getExtraAttrib().putAll(gioElement.getCustomAttributes());
    }

    private void data(GioElementWithData gioElementWithData, GraphmlGraphCommonElement graphmlElement) {
        List<GraphmlData> graphmlDataList = new ArrayList<>();
        gioElementWithData.getDataList().forEach(gioData -> {
            GraphmlData graphmlData = GraphmlData.builder().key(gioData.getKey()).value(gioData.getValue()).build();
            gioData.getId().ifPresent(graphmlData::setId);
            graphmlDataList.add(graphmlData);
        });
        graphmlElement.setDataList(graphmlDataList);
    }

    private void desc(GioElementWithDescription elementWithDescription, GraphmlGraphCommonElement graphmlGraphCommonElement) {
        elementWithDescription.getDescription().ifPresent(desc -> graphmlGraphCommonElement.setDesc(GraphmlDescription.builder().value(desc).build()));
    }

    private GraphmlEndpoint graphmlEndpoint(GioEndpoint endpoint) {
        GraphmlEndpoint graphmlEndpoint = GraphmlEndpoint.builder().id(endpoint.getId()).node(endpoint.getNode()).build();
        endpoint.getPort().ifPresent(graphmlEndpoint::setPort);
        Direction dir = switch (endpoint.getType()) {
            case In -> Direction.In;
            case Out -> Direction.Out;
            case Undirected -> Direction.Undirected;
        };
        graphmlEndpoint.setType(dir);
        return graphmlEndpoint;
    }

    private Optional<GraphmlLocator> locator(Optional<URL> locator) {
        return locator.map(url -> GraphmlLocator.builder().xLinkHref(url).build());
    }

    private GraphmlPort port(GioPort gioPort) {
        GraphmlPort graphmlPort = GraphmlPort.builder().name(gioPort.getName()).build();
        customAttributes(gioPort, graphmlPort);
        // TODO need to copy recursive sub-ports and their data
        // data(gioPort, graphmlPort);
        return graphmlPort;
    }
}
