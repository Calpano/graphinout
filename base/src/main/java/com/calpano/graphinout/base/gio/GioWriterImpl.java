package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.*;

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
    public void endGraph() throws IOException {
        graphmlWriter.endGraph();
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
        if (document.getDataList() != null)
            if (document.getDataList() != null) {
                List<GraphmlData> graphmlDataList = new ArrayList<>();
                graphmlDocument.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
                graphmlDocument.setDataList(graphmlDataList);
            }
        graphmlWriter.startDocument(graphmlDocument);
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
        if (gioGraph.getNodes() != null) {
            for (GioNode gioNode : gioGraph.getNodes())
                startNode(gioNode);
        }
        if (gioGraph.getHyperEdges() != null) {
            for (GioEdge gioEdge : gioGraph.getHyperEdges())
                startEdge(gioEdge);
        }

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

        if(gioNode.getPorts()!=null){
            List<GraphmlPort> graphmlPorts = new ArrayList<>();
            gioNode.getPorts().forEach(gioPort -> graphmlPorts.add(GraphmlPort.builder()
                    .extraAttrib(gioPort.getExtraAttrib())
                    .name(gioPort.getName())
                    .build()));
            graphmlNode.setPorts(graphmlPorts);
        }


    }

    @Override
    public void endNode(Optional<GioLocator> locator) throws IOException {
        // FIXME implement
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        GraphmlHyperEdge hyperEdge = GraphmlHyperEdge.builder()
                .id(edge.getId())
                .extraAttrib(edge.getExtraAttrib())
                .build();

        if (edge.getDataList() != null) {
            List<GraphmlData> graphmlDataList = new ArrayList<>();
            edge.getDataList().forEach(gioData -> graphmlDataList.add(GraphmlData.builder().id(gioData.getId()).key(gioData.getKey()).value(gioData.getValue()).build()));
            hyperEdge.setDataList(graphmlDataList);
        }
        if (edge.getEndpoints() != null) {
            List<GraphmlEndpoint> endpoints = new ArrayList<>();
            edge.getEndpoints().forEach(endpoint -> endpoints.add(GraphmlEndpoint.builder()
                    .id(endpoint.getId())
                    .port(endpoint.getPort())
                    .node(endpoint.getNode())
                    .type(endpoint.getType())
                    .desc(endpoint.getDesc() == null ? null : GraphmlDescription.builder().value(endpoint.getDesc().getValue()).build()).build()));
            hyperEdge.setEndpoints(endpoints);
        }
        graphmlWriter.makeEdge(hyperEdge);
    }

    @Override
    public void endEdge() throws IOException {
        // FIXME implement
    }
}
