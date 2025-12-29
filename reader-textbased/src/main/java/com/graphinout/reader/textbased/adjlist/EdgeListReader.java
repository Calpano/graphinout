package com.graphinout.reader.textbased.adjlist;

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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EdgeListReader implements GioReader, ITextWriter {

    public static final String FORMAT_ID = "edgelist";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Edge List Format", ".edgelist");
    private static final String HASH = "#";
    private final Set<String> nodesCreatedSet = new HashSet<>();
    private @Nullable Consumer<ContentError> errorHandler;
    private ICjStream cjStream;


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

        String sourceId, targetId;
        String dataString;

        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            // Not enough parts for an edge (source and target)
            return;
        }
        sourceId = parts[0];

        String rest = parts[1];
        parts = rest.split("\\s+", 2);
        targetId = parts[0];

        if (parts.length > 1) {
            dataString = parts[1];
        } else {
            dataString = null;
        }


        // create nodes for all parts that have not yet been created
        if (nodesCreatedSet.add(sourceId)) {
            var node = cjStream.createNodeChunk();
            node.id(sourceId);
            cjStream.nodeStart(node);
            cjStream.nodeEnd();
        }
        if (nodesCreatedSet.add(targetId)) {
            var node = cjStream.createNodeChunk();
            node.id(targetId);
            cjStream.nodeStart(node);
            cjStream.nodeEnd();
        }

        var edge = cjStream.createEdgeChunk();
        // endpoints: source -> target
        edge.addEndpoint(ep -> ep.node(sourceId));
        edge.addEndpoint(ep -> ep.node(targetId));
        if (dataString != null) {
            // TODO map better to suitable JSON
            // parse dataString -- is it a python dict or JSON map? -> JSON object
            // single string?
            edge.dataMutable(d -> {
                d.add("data", dataString);
            });
        }
        cjStream.edgeStart(edge);
        cjStream.edgeEnd();
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
        this.cjStream = cjStream;

        // Start document and a single graph to hold nodes and edges
        var doc = cjStream.createDocumentChunk();
        cjStream.documentStart(doc);
        var graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        TextReader.read(content, this);

        cjStream.graphEnd();
        cjStream.documentEnd();
    }


}
