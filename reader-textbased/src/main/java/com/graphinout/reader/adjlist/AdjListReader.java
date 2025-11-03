package com.graphinout.reader.adjlist;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class AdjListReader implements GioReader {

    public static final String FORMAT_ID = "adjlist";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Adjacency List Format", ".adjlist");
    private static final Logger LOGGER = LoggerFactory.getLogger(AdjListReader.class);
    private static final String DELIMITER = " ";
    private static final String HASH = "#";
    private static final String LABEL = "label";
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
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
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, cjStream);
        }
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void processFileContent(final Scanner scanner, final ICjStream cjStream) throws IOException {
        // Start document and a single graph to hold nodes and edges
        var doc = cjStream.createDocumentChunk();
        cjStream.documentStart(doc);
        var graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        Set<String> nodesCreatedSet = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            processLine(line, cjStream, nodesCreatedSet);
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    private void processLine(final String inputLine, final ICjStream cjStream, final Set<String> nodesCreatedSet) throws IOException {
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

}
