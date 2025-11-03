package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.text.ITextWriter;
import com.graphinout.foundation.text.TextReader;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EdgeListReader implements GioReader, ITextWriter {

    public static final String FORMAT_ID = "edgelist";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Edge List Format", ".edgelist");
    private static final String DELIMITER = " ";
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

        String[] parts = line.split(DELIMITER);
        String sourceId = null;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // create nodes for all parts that have not yet been created
            if (nodesCreatedSet.add(part)) {
                var node = cjStream.createNodeChunk();
                node.id(part);
                cjStream.nodeStart(node);
                cjStream.nodeEnd();
            }
            if (i == 0) {
                sourceId = part;
            } else {
                String targetId = parts[i];
                var edge = cjStream.createEdgeChunk();
                // endpoints: source -> target
                final String src = sourceId;
                edge.addEndpoint(ep -> ep.node(src));
                edge.addEndpoint(ep -> ep.node(targetId));
                cjStream.edgeStart(edge);
                cjStream.edgeEnd();
            }
        }
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
