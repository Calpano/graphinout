package com.calpano.graphinout.reader.jgrapht;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.graph6.Graph6Sparse6Importer;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class Graph6Reader implements GioReader {

    private static final Logger log = getLogger(Graph6Reader.class);
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("graph6", "graph6 format", ".g6", ".graph6");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        // here we read the inputStream of sis ...

        Graph6Sparse6Importer<Integer, DefaultEdge> importer = new Graph6Sparse6Importer<>();
        Map<Integer, String> nodeIds = new HashMap<>();
        importer.addVertexAttributeConsumer((pair, att) -> {
            Integer vertex = pair.getFirst();
            String s = pair.getSecond();
            AttributeType attType = att.getType();
            String attValue = att.getValue();
            if (s.equals("ID")) {
                nodeIds.put(vertex, attValue);
            } else {
                log.info("Read " + vertex + "." + s + " = " + attValue + " of " + attType);
            }
        });
        importer.setVertexFactory(i -> i);
        // TODO streaming without using an in-memory graph
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        try {
            importer.importGraph(graph, sis.inputStream());

            // and write the graph to our GioWriter
            writer.startDocument(GioDocument.builder().build());
            writer.startGraph(GioGraph.builder().build());

            for (Integer v : graph.vertexSet()) {
                String nodeId = nodeIds.get(v);
                writer.startNode(GioNode.builder().id(nodeId).build());
                writer.endNode(null);
            }
            for (DefaultEdge e : graph.edgeSet()) {
                Integer source = graph.getEdgeSource(e);
                Integer target = graph.getEdgeTarget(e);
                String s = nodeIds.get(source);
                String t = nodeIds.get(target);
                if(s == null) {
                    throw new IllegalStateException("No nodeId for "+source);
                }
                if(t == null) {
                    throw new IllegalStateException("No nodeId for "+target);
                }
                writer.startEdge(GioEdge.builder().endpoints(Arrays.asList( //
                        GioEndpoint.builder().node(s).build(), //
                        GioEndpoint.builder().node(t).build() //
                )).build());
                writer.endEdge();
            }

            writer.endGraph(null);
            writer.endDocument();
        } catch (ImportException e) {
            if (e.getCause() instanceof IOException ioex) throw ioex;
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + e.getMessage(), null));
        }
    }
}
