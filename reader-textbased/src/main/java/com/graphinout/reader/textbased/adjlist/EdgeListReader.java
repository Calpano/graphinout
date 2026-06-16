package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.text.ITextWriter;
import com.graphinout.base.text.TextReader;
import org.apache.commons.io.IOUtils;

import org.jspecify.annotations.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EdgeListReader implements GioReader, ITextWriter {

    public static final String FORMAT_ID = "edgelist";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Edge List Format", ".edgelist");
    private static final String HASH = "#";
    // buffered while parsing so we can emit all nodes before all edges (valid CJ document order)
    private final Set<String> nodeIds = new LinkedHashSet<>();
    private final List<String[]> edges = new ArrayList<>(); // [source, target, data?]
    private @Nullable Consumer<ContentError> errorHandler;


    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void line(String inputLine) {
        // trim comment
        String line = inputLine;
        int commentIndex = line.indexOf(HASH);
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }

        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            // A lone token is an isolated node (NetworkX edge-list convention)
            nodeIds.add(parts[0]);
            return;
        }
        String sourceId = parts[0];

        String rest = parts[1];
        parts = rest.split("\\s+", 2);
        String targetId = parts[0];
        String dataString = parts.length > 1 ? parts[1] : null;

        nodeIds.add(sourceId);
        nodeIds.add(targetId);
        edges.add(new String[]{sourceId, targetId, dataString});
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }
        processFileContent(content, cjStream);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void processFileContent(final String content, final ICjStream cjStream) {
        nodeIds.clear();
        edges.clear();

        // pass 1: parse, buffering nodes and edges
        TextReader.read(content, this);

        // pass 2: emit a single graph, all nodes first, then all edges (valid CJ order)
        var doc = cjStream.createDocumentChunk();
        cjStream.documentStart(doc);
        var graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        for (String nodeId : nodeIds) {
            var node = cjStream.createNodeChunk();
            node.id(nodeId);
            cjStream.nodeStart(node);
            cjStream.nodeEnd();
        }

        for (String[] e : edges) {
            final String sourceId = e[0];
            final String targetId = e[1];
            final String dataString = e[2];
            var edge = cjStream.createEdgeChunk();
            // edge list lines are directed: source -> target (source is the IN endpoint, target the OUT)
            edge.addEndpoint(ep -> ep.node(sourceId).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(targetId).direction(CjDirection.OUT));
            if (dataString != null) {
                // TODO map better to suitable JSON
                edge.dataMutable(d -> d.add("data", dataString));
            }
            cjStream.edgeStart(edge);
            cjStream.edgeEnd();
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

}
