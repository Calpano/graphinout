package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class TgfReader implements GioReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TgfReader.class);
    private static final String DELIMITER = " ";
    private static final String HASH = "#";
    private static final String LABEL = "label";
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("tgf", "Trivial Graph Format", ".tgf");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }

    private void processFileContent(final Scanner scanner, final GioWriter writer) throws IOException {
        boolean edges = false;
        boolean nodes = false;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Set<String> nodesCreatedSet = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(HASH)) {
                edges = true;
                continue;
            }
            if (!edges) {
                nodes = true;
                processNode(line, writer, nodesCreatedSet);
            } else {
                processEdge(line, writer, nodesCreatedSet);
            }
        }

        writer.endGraph(null);
        writer.endDocument();

        handleWarnings(edges, nodes);
    }

    private void processNode(final String line, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        LOGGER.info("--- nodes:");
        String[] nodeParts = line.split(DELIMITER);
        if (!nodesCreatedSet.contains(nodeParts[0])) {
            nodesCreatedSet.add(nodeParts[0]);
            writer.startNode(GioNode.builder().id(nodeParts[0]).build());
            writer.data(GioData.builder().key(LABEL).value(nodeParts[1]).build());
            writer.endNode(null);
        }
    }

    private void processEdge(final String line, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        LOGGER.info("--- edges:");
        String[] edgeParts = line.split(DELIMITER, 3);
        GioEndpoint sourceEndpoint = GioEndpoint.builder().node(edgeParts[0]).build();
        GioEndpoint targetEndpoint = GioEndpoint.builder().node(edgeParts[1]).build();

        List<GioEndpoint> endpointList = Arrays.asList(sourceEndpoint, targetEndpoint);

        if (edgeParts.length == 2 || edgeParts.length == 3) {
            ensureNodesExist(edgeParts, writer, nodesCreatedSet);
            GioEdge gioEdge = GioEdge.builder().endpoints(endpointList).build();
            if (edgeParts.length == 3) {
                GioData.builder().value(edgeParts[2]).build();
            }
            writer.startEdge(gioEdge);
            writer.endEdge();
        }
    }

    private void ensureNodesExist(final String[] edgeParts, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        for (String nodeId : Arrays.asList(edgeParts[0], edgeParts[1])) {
            if (!nodesCreatedSet.contains(nodeId)) {
                LOGGER.warn("No specified nodes found in the file for edge: " + Arrays.toString(edgeParts) + ". Required nodes have been created.");
                writer.startNode(GioNode.builder().id(nodeId).build());
                writer.endNode(null);
                nodesCreatedSet.add(nodeId);
            }
        }
    }

    private void handleWarnings(final boolean edges, final boolean nodes) {
        if (!edges) {
            if (!nodes) {
                if (errorHandler != null) {
                    errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "No nodes found in file", null));
                }
            } else {
                LOGGER.warn("No edges found in the file.");
            }
        }
    }
}

