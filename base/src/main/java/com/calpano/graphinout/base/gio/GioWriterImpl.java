package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.GraphmlData;
import com.calpano.graphinout.base.graphml.GraphmlDefault;
import com.calpano.graphinout.base.graphml.GraphmlDescription;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
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

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private Optional<GraphmlLocator> locator(Optional<URL> locator) {
        return locator.map(url -> GraphmlLocator.builder().xLinkHref(url).build());
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
    public void endPort() {
        // TODO
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        GraphmlKey graphmlKey = GraphmlKey.builder().id(gioKey.getId()).forType(GraphmlKeyForType.valueOf(gioKey.getForType().name())).attrName(gioKey.getAttrName()).attrType(gioKey.getAttrType()).extraAttrib(gioKey.getExtraAttrib()).build();
        if (gioKey.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            gioKey.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlKey.setDataList(graphmlDataList);
        }
        if (gioKey.getDefaultValue() != null)
            graphmlKey.setDefaultValue(GraphmlDefault.builder().value(gioKey.getDefaultValue().getValue()).defaultType(gioKey.getDefaultValue().getDefaultType()).build());

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
        if (document.getDataList() != null) if (document.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            graphmlDocument.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlDocument.setDataList(graphmlDataList);
        }
        graphmlWriter.startDocument(graphmlDocument);
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        GraphmlHyperEdge hyperEdge = GraphmlHyperEdge.builder().id(edge.getId()).extraAttrib(edge.getExtraAttrib()).build();

        if (edge.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            edge.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            hyperEdge.setDataList(graphmlDataList);
        }
        if (edge.getEndpoints() != null) {
            List<GraphmlEndpoint> endpoints = new ArrayList<>();
            edge.getEndpoints().forEach(endpoint -> endpoints.add(GraphmlEndpoint.builder().id(endpoint.getId()).port(endpoint.getPort()).node(endpoint.getNode()).type(endpoint.getType()).desc(endpoint.getDesc() == null ? null : GraphmlDescription.builder().value(endpoint.getDesc().getValue()).build()).build()));
            hyperEdge.setEndpoints(endpoints);
        }
        graphmlWriter.startHyperEdge(hyperEdge);
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        GraphmlGraph graphmlGraph = GraphmlGraph.builder().id(gioGraph.getId()).edgedefault(gioGraph.isEdgedefault()).build();
        desc(gioGraph, graphmlGraph);

        if (gioGraph.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            gioGraph.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlGraph.setDataList(graphmlDataList);
        }
        graphmlWriter.startGraph(graphmlGraph);
        // TODO instead, remember all nodes ids of the stream, also node ids refered to in edges
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


        if (gioNode.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            gioNode.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlNode.setDataList(graphmlDataList);
        }

        if (gioNode.getPorts() != null) {
            List<GraphmlPort> graphmlPorts = new ArrayList<>();
            gioNode.getPorts().forEach(gioPort -> graphmlPorts.add(GraphmlPort.builder().extraAttrib(gioPort.getExtraAttrib()).name(gioPort.getName()).build()));
            graphmlNode.setPorts(graphmlPorts);
        }

        graphmlWriter.startNode(graphmlNode);
    }

    @Override
    public void startPort(GioPort port) {
        // TODO
    }

    private void desc(GioElementWithDescription elementWithDescription, GraphmlGraphCommonElement graphmlGraphCommonElement) {
        if (elementWithDescription.getDescription().isPresent())){
            String desc = elementWithDescription.getDescription().get();
            graphmlGraphCommonElement.setDesc(GraphmlDescription.builder().value(desc).build());
        }
    }
}
