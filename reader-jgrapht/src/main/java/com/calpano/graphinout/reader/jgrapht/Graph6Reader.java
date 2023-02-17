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
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.graph6.Graph6Sparse6Importer;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
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
        return new GioFileFormat("graph6", "graph6 format", ".g6",".graph6");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        // here we read the inputStream of sis ...

        // TODO streaming
        Graph6Sparse6Importer<String, DefaultEdge> importer = new Graph6Sparse6Importer<>();
        importer.setVertexFactory( i -> ""+i);
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        try {
            importer.importGraph(graph, sis.inputStream());

            // and write the graph to our GioWriter
            writer.startDocument(GioDocument.builder().build());
            writer.startGraph(GioGraph.builder().build());

            for (String v : graph.vertexSet()) {
                writer.startNode(GioNode.builder().id(v).build());
                writer.endNode(null);
            }
            for (DefaultEdge e : graph.edgeSet()) {
                String source = graph.getEdgeSource(e);
                String target = graph.getEdgeTarget(e);
                writer.startEdge(GioEdge.builder().endpoints(Arrays.asList(GioEndpoint.builder().id(source).build(), GioEndpoint.builder().id(target).build())).build());
                writer.endEdge();
            }

            // content errors can be signaled like this

            writer.endGraph(null);
            writer.endDocument();
        } catch (ImportException e) {
            if (e.getCause() instanceof IOException ioex) throw ioex;
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + e.getMessage(), null));
        }
    }
}
