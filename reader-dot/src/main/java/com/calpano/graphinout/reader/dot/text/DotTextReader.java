package com.calpano.graphinout.reader.dot.text;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.rmi.server.LogStream.log;

public class DotTextReader implements GioReader {

    @Override
    public void errorHandler(Consumer<ContentError> errorConsumer) {
        GioReader.super.errorHandler(errorConsumer);
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot", "DOT Text Format");
    }

    @Override
    public boolean isValid(InputSource inputSource) throws IOException {
        return GioReader.super.isValid(inputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        GraphParser parser = new GraphParser(inputSource.inputStream());
        Map<String, GraphNode> nodes = parser.getNodes();
        Map<String, GraphEdge> edges = parser.getEdges();

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        log("--- nodes:");
        for (GraphNode node : nodes.values()) {
            List<GioData> gioDataList = new ArrayList<>();
            Map<String, Object> attributes = node.getAttributes();
            String nodeValue = String.valueOf(attributes.get("value"));
            String nodeKey = String.valueOf(attributes.get("key"));
            GioData gioData = GioData.builder()
                    .key(nodeKey)
                    .value(nodeValue)
                    .build();
            gioDataList.add(gioData);

            writer.startNode(GioNode.builder()
                    .id(node.getId())
                    .dataList(gioDataList)
                    .build());

            log(node.getId() + " " + node.getAttributes());
        }

        log("--- edges:");
        for (GraphEdge edge : edges.values()) {
            List<GioData> gioDataList = new ArrayList<>();
            Map<String, Object> attributes = edge.getAttributes();
            String edgeValue = String.valueOf(attributes.get("value"));
            String edgeKey = String.valueOf(attributes.get("key"));
            GioData gioData = GioData.builder()
                    .key(edgeKey)
                    .value(edgeValue)
                    .build();
            gioDataList.add(gioData);
            writer.startEdge(GioEdge.builder()
                    .id(edge.getId())
                    .dataList(gioDataList)
                    .build());
            log(edge.getNode1().getId() + "->" + edge.getNode2().getId() + " " + edge.getAttributes());
        }
        writer.endGraph();
        writer.endDocument();
    }
}
