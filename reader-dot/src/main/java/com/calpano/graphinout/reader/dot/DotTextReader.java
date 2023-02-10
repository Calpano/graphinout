package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class DotTextReader implements GioReader {

    private static final Logger log = getLogger(DotTextReader.class);

    @Override
    public void errorHandler(Consumer<ContentError> errorConsumer) {
        GioReader.super.errorHandler(errorConsumer);
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot", "DOT Text Format");
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if(inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;

        GraphParser parser = new GraphParser(sis.inputStream());
        Map<String, GraphNode> nodes = parser.getNodes();
        Map<String, GraphEdge> edges = parser.getEdges();

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        log.info("--- nodes:");
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
            log.info(node.getId() + " " + node.getAttributes());
        }

        log.info("--- edges:");
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
            log.info(edge.getNode1().getId() + "->" + edge.getNode2().getId() + " " + edge.getAttributes());
        }
        writer.endGraph();
        writer.endDocument();
    }
}
