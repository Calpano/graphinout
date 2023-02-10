package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.GraphmlData;
import com.calpano.graphinout.base.graphml.GraphmlDefault;
import com.calpano.graphinout.base.graphml.GraphmlDescription;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlEndpoint;
import com.calpano.graphinout.base.graphml.GraphmlGraph;
import com.calpano.graphinout.base.graphml.GraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.GraphmlKey;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlLocator;
import com.calpano.graphinout.base.graphml.GraphmlNode;
import com.calpano.graphinout.base.graphml.GraphmlPort;
import com.calpano.graphinout.base.graphml.GraphmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GioWriterImpl implements GioWriter {

    final GraphmlWriter graphmlWriter;

    public GioWriterImpl(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void data(GioKey gioKey) throws IOException {
        GraphmlKey graphmlKey = GraphmlKey.builder().id(gioKey.getId()).forType(GraphmlKeyForType.valueOf(gioKey.getForType().name())).attrName(gioKey.getAttrName()).attrType(gioKey.getAttrType()).extraAttrib(gioKey.getExtraAttrib()).build();
        if (gioKey.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            gioKey.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            graphmlKey.setDataList(graphmlDataList);
        }
        if (gioKey.getDefaultValue() != null)
            graphmlKey.setDefaultValue(GraphmlDefault.builder().value(gioKey.getDefaultValue().getValue()).defaultType(gioKey.getDefaultValue().getDefaultType()).build());

        if (gioKey.getDesc() != null && !gioKey.getDesc().isEmpty())
            graphmlKey.setDesc(GraphmlDescription.builder().value(gioKey.getDesc()).build());
        graphmlWriter.data(graphmlKey);
    }

    @Override
    public void endDocument() throws IOException {
        graphmlWriter.endDocument();
    }

    @Override
    public void endEdge(Optional<GioLocator> locator) throws IOException {
        GraphmlLocator graphmlLocator = null;
        if (locator.isPresent()) {
            graphmlLocator = GraphmlLocator.builder().locatorExtraAttrib(locator.get().getLocatorExtraAttrib()).xLinkHref(locator.get().getXLinkHref()).xLinkType(locator.get().getXLinkType()).build();

        }
        graphmlWriter.endHyperEdge(Optional.of(graphmlLocator));

    }

    @Override
    public void endGraph() throws IOException {
        graphmlWriter.endGraph();
    }

    @Override
    public void endNode(Optional<GioLocator> locator) throws IOException {
        GraphmlLocator graphmlLocator = null;
        if (locator.isPresent()) {
            graphmlLocator = GraphmlLocator.builder().locatorExtraAttrib(locator.get().getLocatorExtraAttrib()).xLinkHref(locator.get().getXLinkHref()).xLinkType(locator.get().getXLinkType()).build();

        }
        graphmlWriter.endNode(Optional.of(graphmlLocator));

    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        GraphmlDocument graphmlDocument = new GraphmlDocument();
        if (document.desc != null) {
            graphmlDocument.setDesc(new GraphmlDescription(document.desc));
        }
        if (document.getKeys() != null) {
            for (GioKey gioKey : document.getKeys()) {
                data(gioKey);
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

        if (gioGraph.getDesc() != null && !gioGraph.getDesc().isEmpty())
            graphmlGraph.setDesc(new GraphmlDescription(gioGraph.getDesc()));


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

        if (gioNode.getDesc() != null && !gioNode.getDesc().isEmpty())
            graphmlNode.setDesc(new GraphmlDescription(gioNode.getDesc()));


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
}
