package com.graphinout.reader.tgf;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioEndpoint;
import com.graphinout.base.gio.GioEndpointDirection;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.reader.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.XmlFragmentString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class TgfReader implements GioReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TgfReader.class);
    private static final String DELIMITER_REGEX = "\\s+";
    private static final String SECTION_MARKER = "#";
    public static final String FORMAT_ID = "tgf";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Trivial Graph Format", ".tgf");
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            // Emit empty document (no graphs) for empty TGF content to allow exact empty roundtrip
            writer.startDocument(GioDocument.builder().build());
            writer.endDocument();
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }

    private void ensureNodesExist(final String[] edgeParts, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        for (String nodeId : Arrays.asList(edgeParts[0], edgeParts[1])) {
            if (!nodesCreatedSet.contains(nodeId)) {
                // Do NOT auto-create nodes; just warn. Preserve original TGF semantics where edges may reference
                // nodes not present in the node section.
                LOGGER.warn("No specified nodes found in the file for edge: " + Arrays.toString(edgeParts) + ". Not creating nodes.");
                if (errorHandler != null) {
                    errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn, "No nodes found for edge endpoint: " + nodeId, null));
                }
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

    private void processEdge(final String rawLine, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        String line = rawLine.trim();
        if (line.isEmpty()) return;
        String[] edgeParts = line.split(DELIMITER_REGEX, 3);
        if (edgeParts.length < 2) return; // invalid edge line
        // ensure nodes exist
        ensureNodesExist(edgeParts, writer, nodesCreatedSet);

        GioEndpoint sourceEndpoint = GioEndpoint.builder().node(edgeParts[0]).type(GioEndpointDirection.Out).build();
        GioEndpoint targetEndpoint = GioEndpoint.builder().node(edgeParts[1]).type(GioEndpointDirection.In).build();
        List<GioEndpoint> endpointList = Arrays.asList(sourceEndpoint, targetEndpoint);

        GioEdge.GioEdgeBuilder edgeBuilder = GioEdge.builder().endpoints(endpointList);
        if (edgeParts.length == 3 && !edgeParts[2].isBlank()) {
            edgeBuilder.description(XmlFragmentString.ofPlainText(edgeParts[2]));
        }
        writer.startEdge(edgeBuilder.build());
        writer.endEdge();
    }

    private void processFileContent(final Scanner scanner, final GioWriter writer) throws IOException {
        boolean edges = false;
        boolean nodes = false;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Set<String> nodesCreatedSet = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue; // skip empty lines
            }
            if (trimmed.equals(SECTION_MARKER)) {
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

    private void processNode(final String rawLine, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        String line = rawLine.trim();
        if (line.isEmpty()) return;
        String[] nodeParts = line.split(DELIMITER_REGEX, 2);
        if (nodeParts.length == 0 || nodeParts[0].isBlank()) return;
        String nodeId = nodeParts[0];
        if (!nodesCreatedSet.contains(nodeId)) {
            nodesCreatedSet.add(nodeId);
            GioNode.GioNodeBuilder nodeBuilder = GioNode.builder().id(nodeId);
            if (nodeParts.length == 2 && !nodeParts[1].isBlank()) {
                nodeBuilder.description(XmlFragmentString.ofPlainText(nodeParts[1]));
            }
            writer.startNode(nodeBuilder.build());
            writer.endNode(null);
        }
    }

}

