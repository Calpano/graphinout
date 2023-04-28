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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.EventDrivenImporter;
import org.jgrapht.nio.ImportException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

public class JGraphTReader<N> {

    private static final Logger log = getLogger(JGraphTReader.class);

    final InputSource inputSource;
    final EventDrivenImporter<N, Pair<N, N>> importer;
    final GioWriter writer;
    final Consumer<ContentError> errorHandler;

    private final AtomicReference<IOException> exception = new AtomicReference<>();

    private final Map<N, String> nodeIds = new HashMap<>();

    public JGraphTReader(InputSource inputSource, EventDrivenImporter<N, Pair<N, N>> importer, GioWriter writer, Consumer<ContentError> errorHandler) {
        this.inputSource = inputSource;
        this.importer = importer;
        this.writer = writer;
        this.errorHandler = errorHandler;
    }

    public void registerNodeMapper(Function<N, String> nodeToNodeIdFun) {
        importer.addVertexConsumer(v-> {
            String nodeId = nodeToNodeIdFun.apply(v);
            handleNodeId(v, nodeId);
        });
    }
    public void registerNodeMapper(BiFunction<N, Map<String, Attribute>, String> nodeWithAttsToNodeIdFun) {
        importer.addVertexWithAttributesConsumer((v, vatts) -> {
            String nodeId = nodeWithAttsToNodeIdFun.apply(v, vatts);
            handleNodeId(v,nodeId);
        });
    }


    public void read() throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        // here we read the inputStream of sis ...

        importer.addEdgeConsumer(e-> {
            N source = e.getFirst();
            N target = e.getSecond();
            String s = nodeIds.get(source);
            String t = nodeIds.get(target);
            if (s == null) {
                throw new IllegalStateException("No nodeId for " + source);
            }
            if (t == null) {
                throw new IllegalStateException("No nodeId for " + target);
            }
            try {
                writer.startEdge(GioEdge.builder().endpoints(Arrays.asList( //
                        GioEndpoint.builder().node(s).build(), //
                        GioEndpoint.builder().node(t).build() //
                )).build());
                writer.endEdge();
            } catch (IOException ex) {
                if (ex.getCause() instanceof IOException ioex) {
                    if(exception.get() == null) {
                        exception.set(ioex);
                    }
                }
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + ex.getMessage(), null));
            }
        });

        try {
            // and write the graph to our GioWriter
            writer.startDocument(GioDocument.builder().build());
            writer.startGraph(GioGraph.builder().build());

            importer.importInput(sis.inputStream());

            writer.endGraph(null);
            writer.endDocument();
        } catch (ImportException | IOException e) {
            if (e.getCause() instanceof IOException ioex) throw ioex;
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + e.getMessage(), null));
        }
        if(exception.get() != null) throw exception.get();
    }

    private void handleNodeId(N node, String nodeId) {
        nodeIds.put(node, nodeId);
        try {
            writer.startNode(GioNode.builder().id(nodeId).build());
            writer.endNode(null);
        } catch (IOException ex) {
            if (ex.getCause() instanceof IOException ioex) {
                if(exception.get() == null) {
                    exception.set(ioex);
                }
            }
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "JGraphT error: " + ex.getMessage(), null));
        }
    }
}
