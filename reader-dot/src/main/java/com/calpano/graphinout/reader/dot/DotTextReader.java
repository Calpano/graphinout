package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class DotTextReader implements GioReader {

    private static final Logger log = getLogger(DotTextReader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("dot", "DOT Text Format", ".dot", ".gv");
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;

        try {
            GraphParser parser = new GraphParser(sis.inputStream());
            Map<String, GraphNode> nodes = parser.getNodes();
            Map<String, GraphEdge> edges = parser.getEdges();

            writer.startDocument(GioDocument.builder().build());
            writer.startGraph(GioGraph.builder().build());

            log.info("--- nodes:");
            for (GraphNode node : nodes.values()) {
                Map<String, Object> attributes = node.getAttributes();
                String nodeValue = String.valueOf(attributes.get("value"));
                String nodeKey = String.valueOf(attributes.get("key"));
                GioData gioData = GioData.builder().key(nodeKey).value(nodeValue).build();

                writer.startNode(GioNode.builder().id(node.getId()).build());
                writer.data(gioData);
                writer.endNode(null);
                log.info(node.getId() + " " + node.getAttributes());
            }

            log.info("--- edges:");
            for (GraphEdge dotEdge : edges.values()) {
                Map<String, Object> attributes = dotEdge.getAttributes();
                String edgeValue = String.valueOf(attributes.get("value"));
                String edgeKey = String.valueOf(attributes.get("key"));
                GioData gioData = GioData.builder().key(edgeKey).value(edgeValue).build();

                writer.startEdge(GioEdge.builder().id(dotEdge.getId()).endpoint(GioEndpoint.builder().node(dotEdge.getNode1().getId()).build()).endpoint(GioEndpoint.builder().node(dotEdge.getNode2().getId()).build()).build());
                writer.data(gioData);
                writer.endEdge();
                log.info(dotEdge.getNode1().getId() + "->" + dotEdge.getNode2().getId() + " " + dotEdge.getAttributes());
            }
            writer.endGraph(null);
            writer.endDocument();
        } catch (GraphParserException e) {
            ContentError.ErrorLevel errorLevel = ContentError.ErrorLevel.Error;
            String errorMessage = e.getMessage();

            if (errorHandler != null) {
                errorHandler.accept(new ContentError(errorLevel, errorMessage, null));
            }
        }
    }
}
